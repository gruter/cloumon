package org.cloumon.agent.item;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloumon.agent.Agent;
import org.cloumon.agent.item.server.CpuPercMetrics;
import org.cloumon.agent.item.server.DiskUsageMetrics;
import org.cloumon.agent.item.server.MemoryMetrics;
import org.cloumon.agent.item.server.NetworkMetrics;
import org.cloumon.agent.item.server.ServerMetricsManager;
import org.cloumon.thrift.MetricRecord;
import org.cloumon.thrift.MonitorItem;

public class SystemMonitorAdaptor extends TimeEventAdaptor {
  static final Log LOG = LogFactory.getLog(SystemMonitorAdaptor.class);
  public static final Map<String, Class<? extends MetricValue>> typeMap = 
    new HashMap<String, Class<? extends MetricValue>>();
  
  static {
    typeMap.put("cpu", CpuPercMetrics.class);
    typeMap.put("memory", MemoryMetrics.class);
    typeMap.put("memoryPercent", MemoryMetrics.class);
    typeMap.put("swap", MemoryMetrics.class);
    typeMap.put("disk", DiskUsageMetrics.class);
    typeMap.put("network", NetworkMetrics.class);
  }
  
  public SystemMonitorAdaptor(Agent agent, MonitorItem monitorItem) {
    super(agent, monitorItem);
  }

  @Override
  public TimerTask getTimeTask() {
    return new SystemMonitorTask();
  }

  class SystemMonitorTask extends TimerTask {
    @Override
    public void run() {
      ServerMetricsManager manager = null;
      try {
        synchronized(monitorItems) {
          manager = new ServerMetricsManager();

          long collectTime = System.currentTimeMillis();
          //LOG.info("start======================================");
          for (MonitorItem eachItem: monitorItems) {
            try {
              runEachItem(eachItem, manager, collectTime);
            } catch (Exception e) {
              LOG.error(e.getMessage(), e);
            }
          }
          //LOG.info("end======================================");
        }
      } finally {
        if (manager != null) {
          manager.close();
        }
      }
    }
  
    private void runEachItem(MonitorItem item, ServerMetricsManager manager, long collectTime) throws Exception {
      String[] params = item.getParams().split(" ");
      String resourceType = params[0];
      String metricItem = params[1];

      if("process".equals(resourceType)) {
        boolean result = true;
        if ("port".equals(metricItem)) {
          result = manager.existsProcess(params[2], Integer.parseInt(params[3]));
        } else if("name".equals(metricItem)) {
          result = manager.existsProcess(params[2]);
        }
        List<MetricRecord> metricsRecords = new ArrayList<MetricRecord>();
        metricsRecords.add(getMetricValueForProcess(item, result, metricItem, collectTime));
        agent.sendMonitorRecords(metricsRecords);
        return;
      }
      
      if(!typeMap.containsKey(resourceType)) {
        return;
      }

      Class<? extends MetricValue> metricValueTypeClass = typeMap.get(resourceType);
      Method method = ServerMetricsManager.class.getMethod("get" + metricValueTypeClass.getSimpleName());
      Object metricResult = method.invoke(manager);
      
      if (metricResult == null) {
        return;
      }
      List<MetricRecord> metricsRecords = new ArrayList<MetricRecord>();
      
      if (metricResult instanceof List) {
        for (Object eachValue: (List)metricResult) {
          metricsRecords.add(getMetricValue(item, (MetricValue)eachValue, metricItem, collectTime));
        }
      } else {
        metricsRecords.add(getMetricValue(item, (MetricValue)metricResult, metricItem, collectTime));
      }
      
//      DateFormat df = new SimpleDateFormat("mm-dd HH:mm:ss SSS");
//      for(MetricRecord metricRecord: metricsRecords) {
//        LOG.info(item.getItemName() + "," + df.format(new Date(metricRecord.getTimestamp())));
//      }
      agent.sendMonitorRecords(metricsRecords);
    }
    
    
    private MetricRecord getMetricValue(MonitorItem monitorItem, MetricValue metricValue, String metricItem, long collectTime) throws Exception {
      String methodName = "get" + metricItem.substring(0, 1).toUpperCase() + metricItem.substring(1);
      
      Object metricData = metricValue.getClass().getMethod(methodName).invoke(metricValue);
      MetricRecord metricsRecord = createMetricRecordObject(monitorItem);
      metricsRecord.setTimestamp(collectTime);
      metricsRecord.setMonitorData(ByteBuffer.wrap(metricData.toString().getBytes()));
      metricsRecord.setResourceName(metricValue.getResourceName());
      
      return metricsRecord;
    }

    private MetricRecord getMetricValueForProcess(MonitorItem monitorItem, boolean result, String metricItem, long collectTime) throws Exception {
      String[] params = monitorItem.getParams().split(" ");
      
      MetricRecord metricsRecord = createMetricRecordObject(monitorItem);
      metricsRecord.setTimestamp(collectTime);
      metricsRecord.setMonitorData(ByteBuffer.wrap((result ? "1": "0").getBytes()));
      metricsRecord.setResourceName(params[0] + "/" + params[1]);
      
      return metricsRecord;
    }
  }
  
  public boolean equals(Object obj) {
    if ( !(obj instanceof SystemMonitorAdaptor) ) {
      return false;
    }
    
    SystemMonitorAdaptor other = (SystemMonitorAdaptor)obj;
    
    return period == other.getPeriod();
  }
}
