package org.cloumon.collector.dao;

import java.io.IOException;

import org.cloumon.thrift.MetricRecord;

public interface CollectorDAO {
  public void addMetricsValue(String tableName, MetricRecord metricsRecord) throws IOException;
  public void addCurrentMetricsValue(MetricRecord metricsRecord) throws IOException;
}
