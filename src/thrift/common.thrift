namespace java org.cloumon.thrift

struct MonitorItem {
  1: string itemId;
  2: string itemName;
  3: string groupName;
  4: bool defaultItem;
  5: string adaptorClass;
  6: i32 period;
  7: string params;
  8: string description;
  9: string alarm;
}

enum ItemType { 
  DEFAULT, HOST
}

struct HostMonitorItem {
  1: ItemType itemType;
  2: string hostName;
  3: string hostIp;
  4: MonitorItem monitorItem;
}

struct MonitorItemSearchFilter {
  1: string hostName;
}

struct HostSummaryMetrics {
  1: string hostName;
  2: string hostIp;
  3: string cpuLoad;
  4: string cpuUser;
  5: string diskUsed;
  6: string netIn;
  7: string netOut;
  8: string logTime;
  9: bool liveStatus;
}

struct MetricViewRecord {
  1: string itemId;
  2: string itemName;
  3: string groupName;
  4: string hostIp;
  5: string hostName;
  6: string resourceName;
  7: i64 timestamp;
  8: binary monitorData;
  9: string recordId;
}

struct HostHistoryMetricsItem {
  1: string itemName;
  2: string monitorData;
}

struct HostHistoryMetrics {
  1: string logTime;
  2: list<HostHistoryMetricsItem> datas;
}

struct SystemCpuInfo {
  1: string vendor;
  2: string model;
  3: i32 mhz;
  4: i64 cacheSize;
  5: i32 totalCores;
  6: i32 totalSockets;
  7: i32 coresPerSocket;
}

struct SystemFileSystemInfo {
  1: list<string> fileSystems;
}

struct SystemMachineInfo {
  1: string name;
  2: string version;
  3: string arch;
  4: string machine;
  5: string description;
  6: string patchLevel;
  7: string vendor;
  8: string vendorVersion;
  9: string vendorName;
  10: string vendorCodeName;
  11: string dataModel;
  12: string cpuEndian;
  13: string jvmVersion;
  14: string jvmVendor;
  15: string jvmHome;
}

struct SystemMemInfo {
  1: i64 memory;
  2: i64 total;
  3: i64 used;
  4: i64 free;
  5: i64 actualUsed;
  6: i64 actualFree;
  7: i64 swapTotal;
  8: i64 swapUsed;
  9: i64 swapFree;
  10: i64 swapPageIn;
  11: i64 swapPageOut;
}

struct SystemNetworkInterfaceInfo {
  1: list<string> networkInterfaces;
}

struct HostInfo {
  1: string hostName;
  2: string hostType;
  3: list<string> hostIps;
  4: SystemCpuInfo cpuInfo;
  5: SystemFileSystemInfo fileSystemInfo;
  6: SystemMachineInfo machineInfo;
  7: SystemMemInfo memInfo;
  8: SystemNetworkInterfaceInfo networkInfo; 
  9: string alarm; 
 10: bool liveStatus;
 11: bool alarmOn;
 12: string hostAlarm;
}

struct ServiceGroup {
  1: string serviceGroupName;
}

struct MetricRecord {
  1: string itemId;
  2: string itemName;
  3: string groupName;
  4: string hostIp;
  5: string hostName;
  6: string resourceName;
  7: i64 timestamp;
  8: binary monitorData;
  9: string recordId;
}

struct DataNodeStatus {
  1: string hostName;
  2: bool live;
}