package org.cloumon.agent.item;

import java.util.ArrayList;
import java.util.List;

import org.cloumon.agent.Agent;
import org.cloumon.thrift.MetricRecord;
import org.cloumon.thrift.MonitorItem;

public abstract class MonitorItemAdaptor {
  protected List<MonitorItem> monitorItems = new ArrayList<MonitorItem>();
  protected Agent agent;

  public abstract void start();
  public abstract void stop();

  public MonitorItemAdaptor(Agent agent, MonitorItem monitorItem) {
    this.agent = agent;
    this.monitorItems.add(monitorItem);
  }
  
  public List<MonitorItem> getMonitorItems() {
    return monitorItems;
  }
  
  public Agent getAgent() {
    return agent;
  }
  
  public void addMonitorItem(MonitorItem monitorItem) {
    synchronized(monitorItems) {
      monitorItems.add(monitorItem);
    }
  }
  public String getItemNames() {
    String result = "";
    
    for (MonitorItem eachItem: monitorItems) {
      result += eachItem.getItemName() + ",";
    }
    return result;
  }
  
  protected MetricRecord createMetricRecordObject(MonitorItem monitorItem) {
    MetricRecord metricsRecord = new MetricRecord();
    metricsRecord.setHostIp(agent.getHostIp());
    metricsRecord.setHostName(agent.getHostName());
    metricsRecord.setItemId(monitorItem.getItemId());
    metricsRecord.setItemName(monitorItem.getItemName());
    metricsRecord.setGroupName(monitorItem.getGroupName());
    return metricsRecord;
  }

  public int searchMonitorItem(String itemId) {
    int index = -1;
    synchronized(monitorItems) {
      for (MonitorItem eachItem: monitorItems) {
        index++;
        if(itemId.equals(eachItem.getItemId())) {
          return index;
        }
      }
    }
    
    return index;
  }

  public void removeMonitorItem(int index) {
    synchronized(monitorItems) {
      monitorItems.remove(index);
    }
  }
}
