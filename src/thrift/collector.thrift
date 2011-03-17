include "common.thrift"

namespace java org.cloumon.thrift

service CollectorService {
  void addMetricRecord(1: list<common.MetricRecord> metricRecords); 
}