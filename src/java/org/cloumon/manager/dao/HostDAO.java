package org.cloumon.manager.dao;

import java.io.IOException;
import java.util.List;

import org.cloumon.thrift.HostInfo;

public interface HostDAO {
  public void addHost(HostInfo hostInfo) throws IOException;
  public List<HostInfo> findAllHosts() throws IOException;
  public HostInfo findHostByName(String hostName) throws IOException;
  public void removeHost(String hostName) throws IOException;
  public List<HostInfo> findHostByServiceGroup(String serviceGroup) throws IOException;
  public List<HostInfo> findHostByMonitorItem(String monitorItemId) throws IOException;
  public void deleteAll() throws IOException;
  public void updateAgentLiveStatus(String agentHostName, boolean status) throws IOException;
  public void updateAgentAlarm(String agentHostName, boolean on) throws IOException;
  public void updateAgentAlarm(String agentHostName, String alarm, boolean on) throws IOException;
  public void deleteHost(String hostName) throws IOException;
}
