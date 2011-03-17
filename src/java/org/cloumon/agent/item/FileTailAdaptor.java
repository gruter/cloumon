package org.cloumon.agent.item;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloumon.agent.Agent;
import org.cloumon.thrift.MonitorItem;

public class FileTailAdaptor extends TimeEventAdaptor {
  static final Log LOG = LogFactory.getLog(FileTailAdaptor.class);
  
  public FileTailAdaptor(Agent agent, MonitorItem monitorItem) {
    super(agent, monitorItem);
  }

  @Override
  public TimerTask getTimeTask() {
    return new FileTailTask();
  }

  class FileTailTask extends TimerTask {
    @Override
    public void run() {
      synchronized(monitorItems) {
        long collectTime = System.currentTimeMillis();
        //LOG.info("start======================================");
        for (MonitorItem eachItem: monitorItems) {
          try {
            //runEachItem(eachItem, collectTime);
          } catch (Exception e) {
            LOG.error(e.getMessage(), e);
          }
        }
        //LOG.info("end======================================");
      }
    }
  }
}
