package org.cloumon.manager.dao;

import java.io.IOException;
import java.util.List;

import org.cloumon.thrift.HostHistoryMetrics;
import org.cloumon.thrift.MetricViewRecord;

public interface MetricDAO {
  public List<MetricViewRecord> findCurrentMetrics(String groupName) throws IOException;
  public List<MetricViewRecord> findCurrentMetrics(String groupName, String itemName) throws IOException;
  public List<HostHistoryMetrics> findHistoryMetrics(String tableName, String hostName, String groupName, String resourceName, 
      List<String> itemNames, String startTime, String endTime) throws IOException;
  public List<String> getGroupNameByHost(String hostName) throws IOException;
  public List<String> getGroupNameWithResourceByHost(String hostName) throws IOException;
  public List<MetricViewRecord> findCurrentMetricsByHostGroup(String hostName, String eachGroupName) throws IOException;
  public long getMaxMetricRecordId() throws IOException;
  public void deleteAll() throws IOException;
  public void deleteAllCurrentMetricRecord() throws IOException;
  public void createMetricTable(String tableName) throws IOException;
}
