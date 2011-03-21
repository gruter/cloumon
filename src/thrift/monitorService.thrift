include "common.thrift"

namespace java org.cloumon.thrift

service MonitorService {
  //MonitorItem
  void addMinitorItem(1:list<common.MonitorItem> monitorItems);
  void modifyMonitorItem(1:common.MonitorItem monitorItem, 2:bool autoDeployAlarm);
  void removeMonitorItem(1:string monitorItemId);
  list<common.MonitorItem> findAllMonitorItems();
  list<common.MonitorItem> findHostMonitorItems(1:string hostName);
  list<common.MonitorItem> findMonitorItemByGroup(1:string groupName);
  list<string> findHostItemGroup(1:string hostName);
  common.MonitorItem getMonitorItem(1:string itemId);
  void addHostToMonitorItem(1: list<string> itemId, 2: list<string> hostNames);

  void modifyMonitorItemToHosts(1:string monitorItemId, 2:list<string> hostsIs);
  void modifyHostMonitorItem(1:string monitorItemId, 2:string hostName, 3:string alarm);
  
  //HostMetrics
  list<common.HostSummaryMetrics> getHostSummaryMetrics();
  list<common.MetricViewRecord> getHostCurrentMetrics(1:string hostName, 2:string groupName);
  list<common.HostHistoryMetrics> getHostHistoryMetrics(1:string hostName, 2:string groupName, 3:string startTime, 4:string endTime);
  list<common.HostHistoryMetrics> getHostHistoryItemMetrics(1:string hostName, 2:string groupName, 3:list<string> itemNames, 4:string startTime, 5:string endTime);
  string checkMetricHistoryTime(1:string startTime, 2:string endTime);
  
  //HostInfo
  common.HostInfo getHostInfo(1: string hostName);
  void removeHosts(1: list<string> hostNames);
  list<common.HostInfo> findAllHosts();
  list<common.HostInfo> findHostByServiceGroup(1: string serviceGroupName);
  list<common.HostInfo> findHostByMonitorItem(1: string monitorItemId);
  
  void registerAgent(1:string agentIp, 2:common.HostInfo hostInfo);
  void updateAgentLiveStatus(1:string agentHostName, 2:bool liveStatus);
  void updateAgentAlarm(1:string agentHostName, 2: string alarm, 3:bool on); 
  
  //ServiceGroup
  list<common.ServiceGroup> findAllServiceGroup();
  void addServiceGroup(1: common.ServiceGroup serviceGroup);
  void removeServiceGroup(1: string serviceGroupName);
  void removeHostsFromServiceGroup(1: string serviceGroupName, 2: list<string> hostNames);
  void addHostToService(1: string serviceGroupName, 2: list<string> hostNames);
  
  //Hadoop
  list<common.HadoopServerStatus> getHadoopServerList(1:string type);
}