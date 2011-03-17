package org.cloumon.agent.item;

import java.util.Timer;
import java.util.TimerTask;

import org.cloumon.agent.Agent;
import org.cloumon.thrift.MonitorItem;

public abstract class TimeEventAdaptor extends MonitorItemAdaptor {
  protected Timer timer;
  protected String groupName;
  protected int period;
  protected boolean started = false;
  
  public abstract TimerTask getTimeTask();
  
  public TimeEventAdaptor(Agent agent, MonitorItem monitorItem) {
    super(agent, monitorItem);
    this.timer = new Timer(); 
    this.groupName = monitorItem.getGroupName();
    this.period = monitorItem.getPeriod();
  }
  
  @Override
  public void start() {
    if(!started) {
      try {
        //for adding Group Item 
        Thread.sleep(10 * 1000);
      } catch (InterruptedException e) {
      }
      timer.schedule(getTimeTask(), 0L, period * 1000);
      started = true;
    }
  }

  @Override
  public void stop() {
    timer.cancel();
    started = false;
  }

  public String getGroupName() {
    return groupName;
  }
  public int getPeriod() {
    return period;
  }
  
  public boolean equals(Object obj) {
    if ( !(obj instanceof TimeEventAdaptor) ) {
      return false;
    }
    
    TimeEventAdaptor other = (TimeEventAdaptor)obj;
    
    return groupName.equals(other.getGroupName()) && period == other.getPeriod();
  }
}
