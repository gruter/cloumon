package org.cloumon.collector.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloumon.collector.writer.DBWriter;
import org.cloumon.common.util.DBConnectionPool;
import org.cloumon.thrift.MetricRecord;
import org.mortbay.util.StringUtil;

import com.gruter.common.util.StringUtils;
  
public class CollectorDAODBImpl implements CollectorDAO {
  static final Log LOG = LogFactory.getLog(CollectorDAODBImpl.class);
  
  static final SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
  static final String insertSql = " (MetricRecordID, ItemID, ItemName, GroupName, HostName, HostIp, ResourceName, MetricData, CollectTime) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
  
  static final String updateCurrentSql = 
    "UPDATE CurrentMetricRecord " +
    "   SET MetricRecordID = ?, ItemName = ?, GroupName = ?, HostIp = ?, MetricData = ?, CollectTime = ? " +
    " WHERE HostName = ? AND ItemID = ? AND ResourceName = ?";
  
  static final String insertCurrentSql = 
    "INSERT " +
    "  INTO CurrentMetricRecord (ItemID, MetricRecordID, ItemName, GroupName, HostName, HostIp, ResourceName, MetricData, CollectTime) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
  
  @Override
  public void addMetricsValue(String tableName, MetricRecord metricsRecord) throws IOException {
    Connection conn = DBConnectionPool.getConnection(DBWriter.MONITOR_MASTER_DB_POOL_NAME); 
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement("INSERT INTO " + tableName + insertSql);

      int index = 1;
      stmt.setObject(index++, metricsRecord.getRecordId());
      stmt.setObject(index++, metricsRecord.getItemId());
      stmt.setObject(index++, metricsRecord.getItemName());
      stmt.setObject(index++, metricsRecord.getGroupName());
      stmt.setObject(index++, metricsRecord.getHostName());
      stmt.setObject(index++, metricsRecord.getHostIp());
      stmt.setObject(index++, StringUtils.ifNull(metricsRecord.getResourceName(), ""));
      stmt.setObject(index++, new String(metricsRecord.getMonitorData()));
      stmt.setObject(index++, new Timestamp(metricsRecord.getTimestamp()));
      
      stmt.executeUpdate();
    } catch (Exception e) {
      throw new IOException(e.getMessage(), e);
    } finally {
      DBConnectionPool.close(conn, stmt);
    }
  }
  
  @Override
  public void addCurrentMetricsValue(MetricRecord metricsRecord) throws IOException {
    Connection conn = DBConnectionPool.getConnection(DBWriter.MONITOR_MASTER_DB_POOL_NAME); 
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(updateCurrentSql);

      int index = 1;
      stmt.setObject(index++, metricsRecord.getRecordId());
      stmt.setObject(index++, metricsRecord.getItemName());
      stmt.setObject(index++, metricsRecord.getGroupName());
      stmt.setObject(index++, metricsRecord.getHostIp());
      stmt.setObject(index++, new String(metricsRecord.getMonitorData()));
      stmt.setObject(index++, new Timestamp(metricsRecord.getTimestamp()));
      stmt.setObject(index++, metricsRecord.getHostName());
      stmt.setObject(index++, metricsRecord.getItemId());
      stmt.setObject(index++, StringUtils.ifNull(metricsRecord.getResourceName(), ""));
      
      if(stmt.executeUpdate() == 0) {
        insertCurrentMetricsValue(metricsRecord);
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new IOException(e.getMessage(), e);
    } finally {
      DBConnectionPool.close(conn, stmt);
    }
  }
  
  private void insertCurrentMetricsValue(MetricRecord metricsRecord) throws IOException {
    Connection conn = DBConnectionPool.getConnection(DBWriter.MONITOR_MASTER_DB_POOL_NAME); 
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(insertCurrentSql);

      int index = 1;
      stmt.setObject(index++, metricsRecord.getItemId());
      stmt.setObject(index++, metricsRecord.getRecordId());
      stmt.setObject(index++, metricsRecord.getItemName());
      stmt.setObject(index++, metricsRecord.getGroupName());
      stmt.setObject(index++, metricsRecord.getHostName());
      stmt.setObject(index++, metricsRecord.getHostIp());
      stmt.setObject(index++, StringUtils.ifNull(metricsRecord.getResourceName(), ""));
      stmt.setObject(index++, new String(metricsRecord.getMonitorData()));
      stmt.setObject(index++, new Timestamp(metricsRecord.getTimestamp()));
      
      stmt.executeUpdate();
    } catch (Exception e) {
      throw new IOException(e.getMessage(), e);
    } finally {
      DBConnectionPool.close(conn, stmt);
    }
  }
}
