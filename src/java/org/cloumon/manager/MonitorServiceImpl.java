package org.cloumon.manager;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.thrift.TException;
import org.apache.zookeeper.KeeperException;
import org.cloumon.agent.item.ItemManager;
import org.cloumon.agent.item.SystemMonitorAdaptor;
import org.cloumon.manager.alarm.AlarmManager;
import org.cloumon.manager.dao.HostDAO;
import org.cloumon.manager.dao.MetricDAO;
import org.cloumon.manager.dao.MonitorItemDAO;
import org.cloumon.manager.dao.ServiceGroupDAO;
import org.cloumon.manager.model.Alarm;
import org.cloumon.manager.model.DataNodeStatusJson;
import org.cloumon.thrift.DataNodeStatus;
import org.cloumon.thrift.HostHistoryMetrics;
import org.cloumon.thrift.HostInfo;
import org.cloumon.thrift.HostSummaryMetrics;
import org.cloumon.thrift.MetricViewRecord;
import org.cloumon.thrift.MonitorItem;
import org.cloumon.thrift.MonitorService;
import org.cloumon.thrift.ServiceGroup;
import org.springframework.stereotype.Service;

import com.gruter.common.conf.GruterConf;
import com.gruter.common.zk.ZKKeyGen;

@Service("monitorService")
public class MonitorServiceImpl implements MonitorService.Iface {
  static Log LOG = LogFactory.getLog(MonitorServiceImpl.class);
  static final String METRIC_TABLE_NAME = "MetricRecord";

  public static DecimalFormat numberFormat = new DecimalFormat("###,###,###,###.##");
  public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  public static DateFormat queryDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  
  @Resource(name="hostDAO")
  private HostDAO hostDAO;
  
  @Resource(name="metricDAO")
  private MetricDAO metricDAO;

  @Resource(name="serviceGroupDAO")
  private ServiceGroupDAO serviceGroupDAO;

  @Resource(name="monitorItemDAO")
  private MonitorItemDAO monitorItemDAO;

  private Set<String> agentPendingCreation = new HashSet<String>();
  
  private String hostItemEventPath;
  
  private GruterConf conf;
  
  private MonitorManagerServer monitorManagerServer;
  public MonitorServiceImpl() throws IOException {
    conf = new GruterConf();
    conf.addResource("cloumon-default.xml");
    conf.addResource("cloumon-site.xml");

    hostItemEventPath = ItemManager.getHostMonitorItemChangedDir(conf, "");
    (new Thread(new AlarmManager(conf, this))).start();
  }
  
  protected void init(MonitorManagerServer monitorManagerServer) {
    this.monitorManagerServer = monitorManagerServer;
  }
  
  public void clearAllDatas() throws IOException {
    hostDAO.deleteAll();
    metricDAO.deleteAll();
    metricDAO.deleteAllCurrentMetricRecord();
    serviceGroupDAO.deleteAll();
    monitorItemDAO.deleteAll();
  }
  
  @Override
  public void addMinitorItem(List<MonitorItem> monitorItems) throws TException {
    List<String> itemIds = new ArrayList<String>();
    boolean defaultItem = false;
    for(MonitorItem eachMonitorItem: monitorItems) {
      try {
        if(eachMonitorItem.getItemId() == null || eachMonitorItem.getItemId().length() == 0) {
          eachMonitorItem.setItemId("" + ZKKeyGen.getInstance(MonitorManagerServer.getZooKeeper()).getNextSequence("item"));
        }
        
        if(SystemMonitorAdaptor.class.getSimpleName().equals(eachMonitorItem.getAdaptorClass())) {
          eachMonitorItem.setParams(eachMonitorItem.getGroupName() + " " + eachMonitorItem.getItemName());
        }
        monitorItemDAO.addMonitorItem(eachMonitorItem);
        if(eachMonitorItem.isDefaultItem()) {
          defaultItem = true;
        }
        if(defaultItem) {
          itemIds.add(eachMonitorItem.getItemId());
        }
      } catch (IOException e) {
        LOG.error(e.getMessage(), e);
      } 
    }
    
    if(defaultItem) {
      List<HostInfo> hosts = findAllHosts();
      List<String> hostNames = new ArrayList<String>();
      for(HostInfo eachHost: hosts) {
        hostNames.add(eachHost.getHostName());
      }
      addHostToMonitorItem(itemIds, hostNames);
    }
  }

  private void notifyAddItemEventToAgent(String eventPath) {
    try {
      LOG.info("notify item changed event:" + eventPath);
      MonitorManagerServer.getZooKeeper().setData(eventPath, String.valueOf("" + System.currentTimeMillis()).getBytes(), -1);
    } catch(KeeperException.NoNodeException e) {
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

  public List<Alarm> findAllHostAlarms() throws IOException {
    return monitorItemDAO.findAllHostAlarms();
  }

  @Override
  public void removeHosts(List<String> hostNames) throws TException {
    try {
      for(String eachHostName: hostNames) {
        hostDAO.deleteHost(eachHostName);
        monitorItemDAO.deleteHostItems(eachHostName);
      }
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new TException(e.getMessage(), e);
    }
  }
  
  @Override
  public void updateAgentLiveStatus(String agentHostName, boolean status) throws TException {
    try {
      hostDAO.updateAgentLiveStatus(agentHostName, status);
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new TException(e.getMessage(), e);
    }
  }
  
  @Override
  public void updateAgentAlarm(String agentHostName, String alarm, boolean on) throws TException {
    try {
      if(alarm == null || alarm.length() == 0) {
        hostDAO.updateAgentAlarm(agentHostName, on);
      } else {
        hostDAO.updateAgentAlarm(agentHostName, alarm, on);
//        if(updateAlarmToAllMonitorItem) {
//          monitorItemDAO.updateAlarmByHostName(agentHostName, alarm);
//        }
      }
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new TException(e.getMessage(), e);
    }
  }
  
  @Override
  public void registerAgent(String agentIp, HostInfo hostInfo) throws TException {
    synchronized(agentPendingCreation) {
      if(agentPendingCreation.contains(agentIp)) {
        LOG.info(agentIp + " pending creation");
        return;
      }
      agentPendingCreation.add(agentIp);
    }
    try {
      LOG.info("Register agent: " + hostInfo.getHostName() + "," + agentIp);
      
      if(hostDAO.findHostByName(hostInfo.getHostName()) == null) {
        hostDAO.addHost(hostInfo);

        //add default item
        List<MonitorItem> defaultItems = monitorItemDAO.findDefaultMonitorItems();
        for(MonitorItem eachItem: defaultItems) {
          monitorItemDAO.addHostToMonitorItem(eachItem.getItemId(), hostInfo.getHostName(), eachItem.getAlarm());
        }
        notifyAddItemEventToAgent(this.hostItemEventPath + hostInfo.getHostName());
      } else {
        this.updateAgentLiveStatus(hostInfo.getHostName(), true);
      }
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    } finally {
      synchronized(agentPendingCreation) {
        agentPendingCreation.remove(agentIp);
      }
    }
  }

  @Override
  public List<HostSummaryMetrics> getHostSummaryMetrics() throws TException {
    try {
      List<MetricViewRecord> cpuLoadRecords = metricDAO.findCurrentMetrics("cpu", "load");
      List<MetricViewRecord> cpuUserRecords = metricDAO.findCurrentMetrics("cpu", "user");
      List<MetricViewRecord> diskRecords = metricDAO.findCurrentMetrics("disk", "usePercent");
      List<MetricViewRecord> networkRxRecords = metricDAO.findCurrentMetrics("network", "rxBytes");
      List<MetricViewRecord> networkTxRecords = metricDAO.findCurrentMetrics("network", "txBytes");
      
      Map<String, HostSummaryMetrics> hostSummaryMetricsMap = new HashMap<String, HostSummaryMetrics>();
      
      List<HostInfo> hostInfos = findAllHosts();
      Map<String, Boolean> hostLiveStatus = new HashMap<String, Boolean>();
      for(HostInfo eachHostInfo: hostInfos) {
        hostLiveStatus.put(eachHostInfo.getHostName(), eachHostInfo.isLiveStatus());
      }
      
      //CPU Load
      for(MetricViewRecord eachRecord: cpuLoadRecords) {
        HostSummaryMetrics summaryMetrics = hostSummaryMetricsMap.get(eachRecord.getHostName());
        if(summaryMetrics == null) {
          summaryMetrics = new HostSummaryMetrics();
          initHostInfo(summaryMetrics, eachRecord);
          hostSummaryMetricsMap.put(eachRecord.getHostName(), summaryMetrics);
        }
        summaryMetrics.setCpuLoad(new String(eachRecord.getMonitorData()));
      }
      
      //CPU User
      for(MetricViewRecord eachRecord: cpuUserRecords) {
        HostSummaryMetrics summaryMetrics = hostSummaryMetricsMap.get(eachRecord.getHostName());
        if(summaryMetrics == null) {
          summaryMetrics = new HostSummaryMetrics();
          initHostInfo(summaryMetrics, eachRecord);
          hostSummaryMetricsMap.put(eachRecord.getHostName(), summaryMetrics);
        }
        summaryMetrics.setCpuUser(new String(eachRecord.getMonitorData()));
      }

      //DiskUsed
      Map<String, MetricViewRecord> diskUsedMap = new HashMap<String, MetricViewRecord>();
      for(MetricViewRecord eachRecord: diskRecords) {
        MetricViewRecord tempRecord = diskUsedMap.get(eachRecord.getHostName());
        if(tempRecord == null) {
          diskUsedMap.put(eachRecord.getHostName(), eachRecord);
        } else {
          if(Double.parseDouble(new String(eachRecord.getMonitorData())) > Double.parseDouble(new String(tempRecord.getMonitorData()))) {
            diskUsedMap.put(eachRecord.getHostName(), eachRecord);
          }
        }
      }
      for(MetricViewRecord eachRecord: diskUsedMap.values()) {
        HostSummaryMetrics summaryMetrics = hostSummaryMetricsMap.get(eachRecord.getHostName());
        if(summaryMetrics == null) {
          summaryMetrics = new HostSummaryMetrics();
          initHostInfo(summaryMetrics, eachRecord);
          hostSummaryMetricsMap.put(eachRecord.getHostName(), summaryMetrics);
        }
        summaryMetrics.setDiskUsed(new String(eachRecord.getMonitorData()));
      }
      
      //NetIn
      Map<String, MetricViewRecord> netInMap = new HashMap<String, MetricViewRecord>();
      for(MetricViewRecord eachRecord: networkRxRecords) {
        MetricViewRecord tempRecord = netInMap.get(eachRecord.getHostName());
        if(tempRecord == null) {
          netInMap.put(eachRecord.getHostName(), eachRecord);
        } else {
          if(Double.parseDouble(new String(eachRecord.getMonitorData())) > Double.parseDouble(new String(tempRecord.getMonitorData()))) {
            netInMap.put(eachRecord.getHostName(), eachRecord);
          }
        }
      }
      for(MetricViewRecord eachRecord: netInMap.values()) {
        HostSummaryMetrics summaryMetrics = hostSummaryMetricsMap.get(eachRecord.getHostName());
        if(summaryMetrics == null) {
          summaryMetrics = new HostSummaryMetrics();
          initHostInfo(summaryMetrics, eachRecord);
          hostSummaryMetricsMap.put(eachRecord.getHostName(), summaryMetrics);
        }
        summaryMetrics.setNetIn(new String(eachRecord.getMonitorData()));
      }
      
      //NetOut
      Map<String, MetricViewRecord> netOutMap = new HashMap<String, MetricViewRecord>();
      for(MetricViewRecord eachRecord: networkTxRecords) {
        MetricViewRecord tempRecord = netInMap.get(eachRecord.getHostName());
        if(tempRecord == null) {
          netOutMap.put(eachRecord.getHostName(), eachRecord);
        } else {
          if(Double.parseDouble(new String(eachRecord.getMonitorData())) > Double.parseDouble(new String(tempRecord.getMonitorData()))) {
            netOutMap.put(eachRecord.getHostName(), eachRecord);
          }
        }
      }
      for(MetricViewRecord eachRecord: netOutMap.values()) {
        HostSummaryMetrics summaryMetrics = hostSummaryMetricsMap.get(eachRecord.getHostName());
        if(summaryMetrics == null) {
          summaryMetrics = new HostSummaryMetrics();
          initHostInfo(summaryMetrics, eachRecord);
          hostSummaryMetricsMap.put(eachRecord.getHostName(), summaryMetrics);
        }
        summaryMetrics.setNetOut(new String(eachRecord.getMonitorData()));
      }      
      
      List<HostSummaryMetrics> result = new ArrayList<HostSummaryMetrics>();
      result.addAll(hostSummaryMetricsMap.values());
      for(HostSummaryMetrics eachMetrics: result) {
        if(hostLiveStatus.containsKey(eachMetrics.getHostName())) {
          eachMetrics.setLiveStatus(hostLiveStatus.get(eachMetrics.getHostName()));  
        } else {
          eachMetrics.setLiveStatus(false);
        }
      }
      //Order by HostName
      Collections.sort(result, new Comparator<HostSummaryMetrics>() {
        @Override
        public int compare(HostSummaryMetrics obj1, HostSummaryMetrics obj2) {
          return obj1.getHostName().compareTo(obj2.getHostName());
        }
      });
      return result;
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new TException(e.getMessage(), e);    
    }
  }

  private void initHostInfo(HostSummaryMetrics summaryMetrics, MetricViewRecord eachRecord) {
    summaryMetrics.setHostName(eachRecord.getHostName());
    summaryMetrics.setHostIp(eachRecord.getHostIp());
    summaryMetrics.setLogTime(dateFormat.format(eachRecord.getTimestamp()));
  }
  
  @Override
  public void addHostToService(String serviceGroupName, List<String> hostNames) throws TException {
    try {
      for(String eachHostName: hostNames) {
        serviceGroupDAO.addHostToServiceGroup(serviceGroupName, eachHostName);
      }
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }
  }

  @Override
  public void addServiceGroup(ServiceGroup serviceGroup) throws TException {
    try {
      serviceGroupDAO.addServiceGroup(serviceGroup);
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }
  }

  @Override
  public List<HostInfo> findAllHosts() throws TException {
    try {
      return hostDAO.findAllHosts();
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }
  }

  @Override
  public List<ServiceGroup> findAllServiceGroup() throws TException {
    try {
      return serviceGroupDAO.findAllServiceGroups();
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }
  }

  @Override
  public List<HostInfo> findHostByServiceGroup(String serviceGroupName) throws TException {
    try {
      if(serviceGroupName == null || serviceGroupName.trim().length() == 0) {
        return findAllHosts();
      } else {
        return hostDAO.findHostByServiceGroup(serviceGroupName);
      }
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }
  }

  @Override
  public void removeServiceGroup(String serviceGroupName) throws TException {
    try {
      serviceGroupDAO.removeServiceGroup(serviceGroupName);
      serviceGroupDAO.removeServiceHosts(serviceGroupName);
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }      
  }

  @Override
  public void removeHostsFromServiceGroup(String serviceGroupName, List<String> hostNames) throws TException {
    try {
      for(String eachHost: hostNames) {
        serviceGroupDAO.removeHostToServiceGroup(serviceGroupName, eachHost);
      }
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }      
  }
  
  @Override
  public List<DataNodeStatus> getDataNodeList() throws TException {
    List<DataNodeStatus> datanodes = new ArrayList<DataNodeStatus>();
    List<DatanodeInfo> liveDataNodes = monitorManagerServer.hadoopMonitorItemLoader.getLiveDataNodes();
    if(liveDataNodes != null) {
      for(DatanodeInfo eachNode: liveDataNodes) {
        DataNodeStatus status = new DataNodeStatusJson();
        status.setHostName(eachNode.getHostName());
        status.setLive(true);
        datanodes.add(status);
      }
    }
    
    List<DatanodeInfo> deadDataNodes = monitorManagerServer.hadoopMonitorItemLoader.getDeadDataNodes();
    if(deadDataNodes != null) {
      for(DatanodeInfo eachNode: deadDataNodes) {
        DataNodeStatus status = new DataNodeStatusJson();
        status.setHostName(eachNode.getHostName());
        status.setLive(false);
        datanodes.add(status);
      }
    }
    
    return datanodes;
  }
  
  public List<MonitorItem> findHostMonitorItemsByGroup(String hostName, String groupName) throws IOException {
    return monitorItemDAO.findHostMonitorItemsByGroup(hostName, groupName);
  }
  
  @Override
  public List<MonitorItem> findAllMonitorItems() throws TException {
    try {
      return monitorItemDAO.findAllMonitorItems();
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }
  }
  
  @Override
  public List<MonitorItem> findMonitorItemByGroup(String groupName) throws TException {
    try {
      return monitorItemDAO.findMonitorItemByGroup(groupName);
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }
  }

  @Override
  public List<MonitorItem> findHostMonitorItems(String hostName) throws TException {
//    try {
//      return monitorItemDAO.findHostMonitorItems(hostName);
//    } catch (IOException e) {
//      throw new TException(e.getMessage(), e);
//    }
    try {
      Set<MonitorItem> items = new HashSet<MonitorItem>();
      
      items.addAll(monitorItemDAO.findHostMonitorItems(hostName));
      //items.addAll(monitorItemDAO.findDefaultMonitorItems());

      List<MonitorItem> result = new ArrayList<MonitorItem>();
      result.addAll(items);
      
      return result;
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }
  }

  @Override
  public void modifyMonitorItem(MonitorItem monitorItem, boolean autoDeployAlarm) throws TException {
    try {
      monitorItemDAO.updateMonitorItem(monitorItem);
      List<HostInfo> hosts = this.findHostByMonitorItem(monitorItem.getItemId());
      
      if(autoDeployAlarm) {
        for(HostInfo eachHost: hosts) {
          monitorItemDAO.updateHostMonitorItem(monitorItem.getItemId(), eachHost.getHostName(), monitorItem.getAlarm());
        }
      }
      for(HostInfo eachHost: hosts) {
        notifyAddItemEventToAgent(hostItemEventPath + eachHost.getHostName());
      }
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }   
  }

  @Override
  public void modifyHostMonitorItem(String monitorItemId, String hostName, String alarm) throws TException {
    try {
      monitorItemDAO.updateHostMonitorItem(monitorItemId, hostName, alarm);
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }   
  }
  
  @Override
  public void modifyMonitorItemToHosts(String monitorItemId, List<String> hostsNames) throws TException {
  }

  @Override
  public void removeMonitorItem(String monitorItemId) throws TException {
    try {
      monitorItemDAO.deleteMonitorItem(monitorItemId);
      List<HostInfo> hosts = this.findHostByMonitorItem(monitorItemId);
      for(HostInfo eachHost: hosts) {
        notifyAddItemEventToAgent(hostItemEventPath + eachHost.getHostName());
      }
      monitorItemDAO.deleteHostMonitorItem(monitorItemId);
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }   
  }

  @Override
  public void addHostToMonitorItem(List<String> itemIds, List<String> hostNames) throws TException {
    for(String eachItemId: itemIds) {
      try {
        addHostToMonitorItem(eachItemId, hostNames);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }
    for(String eachHostName: hostNames) {
      this.notifyAddItemEventToAgent(this.hostItemEventPath + eachHostName);
    }
  }
  
  private void addHostToMonitorItem(String itemId, List<String> hostNames) throws IOException {
    try {
//      List<HostInfo> hostInfos = findHostByMonitorItem(itemId);
//      
      Set<String> hostNameSet = new HashSet<String>();
      hostNameSet.addAll(hostNames);
//      
//      List<String> removeTargets = new ArrayList<String>();
//      for(HostInfo eachHostInfo: hostInfos) {
//        if(!hostNameSet.remove(eachHostInfo.getHostName())) {
//          removeTargets.add(eachHostInfo.getHostName());
//        }
//      }

      for(String eachHostName: hostNameSet) {
        monitorItemDAO.removeHostFromMonitorItem(itemId, eachHostName);
      }
      
      MonitorItem item = getMonitorItem(itemId);

      for(String eachHostName: hostNameSet) {
        monitorItemDAO.addHostToMonitorItem(itemId, eachHostName, item.getAlarm());
      }

    } catch (Exception e) {
      throw new IOException(e.getMessage(), e);
    }   
  }

  @Override
  public MonitorItem getMonitorItem(String itemId) throws TException {
    try {
      return monitorItemDAO.findMonitorItemById(itemId);
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }
  }
  
  @Override
  public List<HostInfo> findHostByMonitorItem(String monitorItemId) throws TException {
    try {
      if(monitorItemId == null || monitorItemId.trim().length() == 0) {
        return findAllHosts();
      } else {
        return hostDAO.findHostByMonitorItem(monitorItemId);
      }
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }
  }

  @Override
  public List<MetricViewRecord> getHostCurrentMetrics(String hostName, String groupName) throws TException {
    try {
      List<MetricViewRecord> result = new ArrayList<MetricViewRecord>();
      
      List<String> groupNames = null;
      if(groupName == null || groupName.length() == 0) {
        groupNames = metricDAO.getGroupNameByHost(hostName);
      } else {
        groupNames = Arrays.asList(new String[]{groupName});
      }
      
      for(String eachGroupName: groupNames) {
        result.addAll(metricDAO.findCurrentMetricsByHostGroup(hostName, eachGroupName));
      }
      
      Collections.sort(result, new Comparator<MetricViewRecord>() {
        @Override
        public int compare(MetricViewRecord o1, MetricViewRecord o2) {
          String compareStr1 = o1.getGroupName() + o1.getResourceName() + o1.getItemName() + o1.getTimestamp();
          String compareStr2 = o2.getGroupName() + o2.getResourceName() + o2.getItemName() + o2.getTimestamp();
          
          return compareStr1.compareTo(compareStr2);
        }
      });
      return result;
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }
  }

  @Override
  public List<HostHistoryMetrics> getHostHistoryMetrics(String hostName, String groupName, String startTime, String endTime)
      throws TException {
    try {
      List<MonitorItem> items = findHostMonitorItems(hostName);
      List<String> itemNames = new ArrayList<String>();
      
      String realGroupName = null;
      String resourceName = null;
      String[] groupToken = groupName.split(":");
      if(groupToken.length > 1) {
        realGroupName = groupToken[0];
        resourceName = groupToken[1];
      } else {
        realGroupName = groupName;
      }
      for(MonitorItem eachItem: items) {
        if(realGroupName.equals(eachItem.getGroupName())) {
          itemNames.add(eachItem.getItemName());
        }
      }
      if(startTime == null || startTime.length() == 0) {
        Date twoHourBeforeDate = new Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000);
        startTime = queryDateFormat.format(twoHourBeforeDate);
      }
      if(endTime == null || endTime.length() == 0) {
        endTime = queryDateFormat.format(new Date(System.currentTimeMillis()));
      }
      if(hostName.equals("namenode")) {
        hostName = monitorManagerServer.hadoopMonitorItemLoader.getNameNodeHost();
      }
      //String rearrangedEndTime = checkMetricHistoryTime(startTime, endTime);
      return metricDAO.findHistoryMetrics(getMetricTableName(endTime), hostName, realGroupName, resourceName, itemNames, startTime, endTime);
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new TException(e.getMessage(), e);
    }
  }

  @Override
  public List<HostHistoryMetrics> getHostHistoryItemMetrics(String hostName, String groupName, List<String> itemNames, String startTime, String endTime)
      throws TException {
    try {
      String realGroupName = null;
      String resourceName = null;
      String[] groupToken = groupName.split(":");
      if(groupToken.length > 1) {
        realGroupName = groupToken[0];
        resourceName = groupToken[1];
      } else {
        realGroupName = groupName;
      }
      if(startTime == null || startTime.length() == 0) {
        Date twoHourBeforeDate = new Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000);
        startTime = queryDateFormat.format(twoHourBeforeDate);
      }
      if(endTime == null || endTime.length() == 0) {
        endTime = queryDateFormat.format(new Date(System.currentTimeMillis()));
      }
      if(hostName.equals("namenode")) {
        hostName = monitorManagerServer.hadoopMonitorItemLoader.getNameNodeHost();
      }
      return metricDAO.findHistoryMetrics(getMetricTableName(endTime), hostName, realGroupName, resourceName, itemNames, startTime, endTime);
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new TException(e.getMessage(), e);
    }
  }
  
  /**
   * 4시간 범위내에서만 검색이 되도록 제한
   * @param startTime
   * @param endTime
   * @return
   * @throws IOException
   */
  @Override
  public String checkMetricHistoryTime(String startTime, String endTime) throws TException {
    try {
      Date startTimeDate = queryDateFormat.parse(startTime);
      Date endTimeDate = queryDateFormat.parse(endTime);
      
      if(endTimeDate.getTime() - startTimeDate.getTime() <= 0 || endTimeDate.getTime() - startTimeDate.getTime() >= 4 * 60 * 60 * 1000) {  //4 hour
        return queryDateFormat.format(startTimeDate.getTime() + (4 * 60 * 60 * 1000));
      } else {
        return endTime;
      }
    } catch (Exception e) {
      throw new TException(e.getMessage(), e);
    }
  }
  
  private String getMetricTableName(String endTime) throws IOException {
    if(endTime.length() < 8) {
      throw new IOException("Wrong date format:" + endTime);
    }
    return METRIC_TABLE_NAME + "_" + endTime.substring(0,4) + endTime.substring(5,7);
  }
  
  @Override
  public List<String> findHostItemGroup(String hostName) throws TException {
    try {
      return metricDAO.getGroupNameWithResourceByHost(hostName);
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }
  }

  @Override
  public HostInfo getHostInfo(String hostName) throws TException {
    try {
      return hostDAO.findHostByName(hostName);
    } catch (IOException e) {
      throw new TException(e.getMessage(), e);
    }
  }

  public long getMaxMetricRecordId() throws IOException {
    return metricDAO.getMaxMetricRecordId();
  }

  public void createTable() throws IOException {
    DateFormat df = new SimpleDateFormat("yyyyMM");
    String currentMonth = df.format(System.currentTimeMillis());
    
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, 1);
    String nextMonth = df.format(cal.getTime());
    
    metricDAO.createMetricTable(METRIC_TABLE_NAME + "_" + currentMonth);
    metricDAO.createMetricTable(METRIC_TABLE_NAME + "_" + nextMonth);
  }
  
  public static void main(String[] args) throws Exception {
    System.out.println(">>>" + new Date(1290753565000L));
  }
}
