include "common.thrift"

namespace java org.cloumon.thrift

service AgentService {
  void addMonitorItems(1:list<common.MonitorItem> monitorItems);
}