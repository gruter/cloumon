package org.cloumon.manager.dao;

import java.io.IOException;
import java.util.List;

import org.cloumon.manager.model.Alarm;
import org.cloumon.thrift.MonitorItem;

public interface MonitorItemDAO {

  public List<MonitorItem> findAllMonitorItems() throws IOException;

  public List<MonitorItem> findHostMonitorItems(String hostName) throws IOException;

  public List<Alarm> findAllHostAlarms() throws IOException;

  public void addMonitorItem(MonitorItem monitorItem) throws IOException;

  public void addHostToMonitorItem(String itemId, String hostName, String alarm) throws IOException;

  public void removeHostFromMonitorItem(String itemId, String hostName) throws IOException;

  public List<MonitorItem> findDefaultMonitorItems() throws IOException;

  public void deleteMonitorItem(String monitorItemId) throws IOException;
  
  public void deleteHostMonitorItem(String monitorItemId) throws IOException;

  public MonitorItem findMonitorItemById(String itemId)  throws IOException;

  public void updateMonitorItem(MonitorItem monitorItem) throws IOException;

  public void updateHostMonitorItem(String monitorItemId, String hostName, String alarm) throws IOException;
  
  public void deleteAll() throws IOException;

  public void updateAlarmByHostName(String agentHostName, String alarm) throws IOException;

  public void deleteHostItems(String hostName) throws IOException;

  public List<MonitorItem> findHostMonitorItemsByGroup(String hostName, String groupName)  throws IOException;

  public List<MonitorItem> findMonitorItemByGroup(String groupName) throws IOException;
}
