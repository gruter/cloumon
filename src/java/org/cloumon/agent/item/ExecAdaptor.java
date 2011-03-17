package org.cloumon.agent.item;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloumon.agent.Agent;
import org.cloumon.common.util.ExecPlugin;
import org.cloumon.thrift.MetricRecord;
import org.cloumon.thrift.MonitorItem;
import org.json.JSONException;
import org.json.JSONObject;

public class ExecAdaptor extends TimeEventAdaptor {
  private static final Log LOG = LogFactory.getLog(ExecAdaptor.class);
  public static final boolean FULL_PATHS = false;

  private List<EmbeddedExec> runningExecs = new ArrayList<EmbeddedExec>();

  public ExecAdaptor(Agent agent, MonitorItem monitorItem) {
    super(agent, monitorItem);
  }

  @Override
  public TimerTask getTimeTask() {
    return new ExecTimerTask();
  }

  static class EmbeddedExec extends ExecPlugin {
    String cmd;

    public EmbeddedExec(String cmd) {
      this.cmd = cmd;
    }

    @Override
    public String getCmde() {
      return cmd;
    }
  }

  class ExecTimerTask extends TimerTask {
    public void run() {
      synchronized(monitorItems) {
        long collectTime = System.currentTimeMillis();
        runningExecs.clear();
        for (MonitorItem eachItem: monitorItems) {
          EmbeddedExec exec = new EmbeddedExec(eachItem.getParams());
          
          runningExecs.add(exec);
          runExec(eachItem, exec, collectTime);
        }
      }
    }
    
    public void runExec(MonitorItem monitorItem, EmbeddedExec embeddedExec, long collectTime) {
      LOG.info("Exec monitor item:" + monitorItem.getItemName());
      JSONObject o = embeddedExec.execute();
      try {

        if (o.getInt("status") == embeddedExec.statusKO) {
          // deregisterAndStop();
          return;
        }

        String stdout = o.getString("stdout");

        MetricRecord metricsRecord = createMetricRecordObject(monitorItem);
        metricsRecord.setTimestamp(collectTime);
        metricsRecord.setMonitorData(ByteBuffer.wrap(stdout.trim().getBytes()));
        List<MetricRecord> metricsRecords = new ArrayList<MetricRecord>();
        metricsRecords.add(metricsRecord);
        agent.sendMonitorRecords(metricsRecords);
      } catch (Exception e) {
        LOG.warn(e);
      }
    }
  };

  @Override
  public void stop() {
    super.stop();
    for (EmbeddedExec eachExec: runningExecs) {
      eachExec.stop();
    }
  }

//  public String parseArgs(String status) {
//    int spOffset = status.indexOf(' ');
//    if (spOffset > 0) {
//      try {
//        period = Integer.parseInt(status.substring(0, spOffset));
//        cmd = status.substring(spOffset + 1);
//      } catch (NumberFormatException e) {
//        LOG.warn("ExecAdaptor: sample interval " + status.substring(0, spOffset) + " can't be parsed");
//        cmd = status;
//      }
//    } else {
//      cmd = status;
//    }
//
//    return cmd;
//  }

  public static void main(String[] args) {
    EmbeddedExec exec = new EmbeddedExec("ps ax | grep java | wc -l");
    JSONObject o = exec.execute();
    try {

      if (o.getInt("status") == exec.statusKO) {
        return;
      }

      String stdout = o.getString("stdout");
      if (stdout != null) {
        stdout = stdout.trim();
      }
      //byte[] data = stdout.getBytes();

      System.out.println("stdout: " + stdout);
    } catch (JSONException e) {
      LOG.warn(e);
    }
  }
}