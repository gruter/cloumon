package org.cloumon.manager.dao;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cloumon.manager.model.HostHistoryMetricsJson;
import org.cloumon.manager.model.MetricViewRecordJson;
import org.cloumon.thrift.HostHistoryMetrics;
import org.cloumon.thrift.HostHistoryMetricsItem;
import org.cloumon.thrift.MetricViewRecord;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("metricDAO")
public class MetricDAOImpl extends MonitorJdbcDaoSupport implements MetricDAO {
  static final String selectMetricRecordWithResourceSql = 
    "SELECT a.HostName, a.HostIp, a.ItemId, a.ItemName, a.GroupName, a.MetricData, a.ResourceName, a.CollectTime \n" +
    "  FROM MetricRecord as a \n" +
    " WHERE a.HostName = ? \n" +
    "   AND a.groupName = ? \n" +
    "   AND a.resourceName = ? \n" +
    "   AND date_format(a.CollectTime, '%Y%m%d%H%i') BETWEEN ? AND ? ORDER BY a.CollectTime asc";
  
  static final String selectCurrentByGroupSql = 
    "SELECT HostName, HostIp, ItemId, ItemName, GroupName, MetricData, ResourceName, CollectTime \n" +
    "  FROM CurrentMetricRecord \n" + 
    " WHERE GroupName = ? ORDER BY HostName";  

  static final String selectCurrentByItemSql = 
    "SELECT HostName, HostIp, ItemId, ItemName, GroupName, MetricData, ResourceName, CollectTime \n" +
    "  FROM CurrentMetricRecord \n" +
    " WHERE GroupName = ? and ItemName = ? ORDER BY HostName ";    

  static final String selectCurrentByHostGroupSql = 
    "SELECT HostName, HostIp, ItemId, ItemName, GroupName, MetricData, ResourceName, CollectTime \n" +
    "  FROM CurrentMetricRecord \n" +
    " WHERE HostName = ? AND GroupName = ? ORDER BY GroupName \n";    

  static final String selectGroupNameSql = 
    "SELECT GroupName FROM CurrentMetricRecord WHERE HostName = ? GROUP BY GroupName";
  
  static final String selectGroupNameWithResouceSql = 
    "SELECT GroupName, ResourceName FROM CurrentMetricRecord WHERE HostName = ? GROUP BY GroupName, ResourceName";
  
  static final String selectMaxRecordIdSql = 
    "SELECT IFNULL(MAX(MetricRecordID), -1) FROM MetricRecord";
  
  static final String deleteAllSql = "DELETE FROM MetricRecord";
  
  static final String deleteAllCurrentMetricRecordSql = "DELETE FROM CurrentMetricRecord";
  
  static final String createTableSql = 
    "  MetricRecordID  BIGINT       NOT NULL, \n" + 
    "  ItemID          VARCHAR( 20), \n" + 
    "  ItemName        VARCHAR( 50) NOT NULL, \n" + 
    "  GroupName       VARCHAR( 50) , \n" +  
    "  HostName        VARCHAR(100) NOT NULL,\n" + 
    "  HostIp          VARCHAR(255) NOT NULL,\n" + 
    "  ResourceName    VARCHAR(255) , \n" + 
    "  MetricData      TEXT,\n" + 
    "  CollectTime     DATETIME NOT NULL,\n" + 
    "  PRIMARY KEY(MetricRecordID)\n" + 
    ") DEFAULT CHARSET='utf8'";
  
  @Override
  public List<String> getGroupNameByHost(String hostName) throws IOException {
    return getJdbcTemplate().query(selectGroupNameSql, new RowMapper<String>() {
      @Override
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString(1);
      }
    }, hostName);
  }

  @Override
  public List<String> getGroupNameWithResourceByHost(String hostName) throws IOException {
    return getJdbcTemplate().query(selectGroupNameWithResouceSql, new RowMapper<String>() {
      @Override
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        String groupName = rs.getString(1);
        String resourceName = rs.getString(2);
        if(resourceName != null && resourceName.length() > 0) {
          return groupName + ":" + resourceName;
        } else {
          return groupName;
        }
      }
    }, hostName);
  }
  
  @Override
  public List<MetricViewRecord> findCurrentMetrics(String groupName) throws IOException {
    return getJdbcTemplate().query(selectCurrentByGroupSql, new MetricRecordRowMapper(), groupName);
  }
  
  @Override
  public List<MetricViewRecord> findCurrentMetricsByHostGroup(String hostName, String groupName) throws IOException {
    return getJdbcTemplate().query(selectCurrentByHostGroupSql, new MetricRecordRowMapper(), hostName, groupName);
  }
  
  @Override
  public List<MetricViewRecord> findCurrentMetrics(String groupName, String itemName) throws IOException {
    return getJdbcTemplate().query(selectCurrentByItemSql, new MetricRecordRowMapper(), groupName, itemName);
  }
  
  @Override
  public List<HostHistoryMetrics> findHistoryMetrics(String tableName, String hostName, String groupName, 
      String resourceName, List<String> itemNames, String startTime, String endTime)
      throws IOException {
    String sql = "SELECT CollectTime ";
    
    for(String eachItemName: itemNames) {
      sql += ",\n MAX(CASE ItemName WHEN '" + eachItemName + "' THEN MetricData ELSE 0 END) ";
    }
    
    sql +=  " FROM " + tableName + " \n" +
            "WHERE HostName = ? AND groupName = ? \n";
    
    if(resourceName != null) {
      sql += "  AND resourceName = ? \n";
      sql += "  AND date_format(CollectTime, '%Y-%m-%d %H:%i') BETWEEN ? AND ? \n";
      sql += "GROUP BY CollectTime ORDER BY CollectTime desc";
      System.out.println(sql + "," + hostName + "," + groupName + "," + resourceName + "," + startTime + "," + endTime);
      return getJdbcTemplate().query(sql, new HostHistoryMetricsRowMapper(itemNames), hostName, groupName, resourceName, startTime, endTime);
    } else {
      sql += "  AND date_format(CollectTime, '%Y-%m-%d %H:%i') BETWEEN ? AND ? \n";
      sql += "GROUP BY CollectTime ORDER BY CollectTime desc";
      System.out.println(sql + "," + hostName + "," + groupName + "," + startTime + "," + endTime);
      return getJdbcTemplate().query(sql, new HostHistoryMetricsRowMapper(itemNames), hostName, groupName, startTime, endTime);
    }
  }

  class HostHistoryMetricsRowMapper implements RowMapper<HostHistoryMetrics> {
    private List<String> itemNames;
    private int itemCount;
    public HostHistoryMetricsRowMapper(List<String> itemNames) {
      this.itemNames = itemNames;
      this.itemCount = itemNames.size();
    }
    @Override
    public HostHistoryMetrics mapRow(ResultSet rs, int rowNum) throws SQLException {
      HostHistoryMetrics result = new HostHistoryMetricsJson();
      int index = 1;
      result.setLogTime(rs.getString(index++));
      
      List<HostHistoryMetricsItem> datas = new ArrayList<HostHistoryMetricsItem>();
      for(int i = 0; i< itemCount; i++) {
        HostHistoryMetricsItem data = new HostHistoryMetricsItem();
        data.setItemName(itemNames.get(i));
        data.setMonitorData(rs.getString(index++));
        datas.add(data);
      }
      result.setDatas(datas);
      return result;
    }
  }
  
  class MetricRecordRowMapper implements RowMapper<MetricViewRecord> {
    @Override
    public MetricViewRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
      MetricViewRecord record = new MetricViewRecordJson();
      record.setItemId(rs.getString("ItemId"));
      record.setItemName(rs.getString("ItemName"));
      record.setGroupName(rs.getString("GroupName"));
      record.setHostName(rs.getString("HostName"));
      record.setHostIp(rs.getString("HostIp"));
      record.setResourceName(rs.getString("ResourceName"));
      record.setMonitorData(ByteBuffer.wrap(rs.getBytes("MetricData")));
      record.setTimestamp(rs.getTimestamp("CollectTime").getTime());  
      
      return record;
    }
  }

  @Override
  public long getMaxMetricRecordId() throws IOException {
    return getJdbcTemplate().queryForLong(selectMaxRecordIdSql);
  }  
  
  @Override
  public void deleteAll() throws IOException {
    getJdbcTemplate().execute(deleteAllSql);
  }
  
  public void deleteAllCurrentMetricRecord() throws IOException {
    getJdbcTemplate().execute(deleteAllCurrentMetricRecordSql);
  }
  
  @Override
  public void createMetricTable(String tableName) throws IOException {
    if(!existsTable(tableName)) {
      getJdbcTemplate().execute("CREATE TABLE " + tableName + " ( \n" + createTableSql);
      getJdbcTemplate().execute("CREATE INDEX " + tableName + "_IX01 ON " + tableName + " (CollectTime) ");    
    }
  }
  
  private boolean existsTable(final String tableName) throws IOException {
    return (Boolean) getJdbcTemplate().execute(new ConnectionCallback<Boolean>() {
        @Override
        public Boolean doInConnection(Connection conn) throws SQLException, DataAccessException {
          DatabaseMetaData mtdt = conn.getMetaData();

          ResultSet rs = mtdt.getTables(conn.getCatalog(), "%", "MetricRecord%", new String[]{"TABLE"});
          
          boolean matched = false;
          while (rs.next()) {
            if(tableName.toLowerCase().equals(rs.getString("TABLE_NAME").toLowerCase())) {
              matched = true;
              break;
            }
          }
          
          return matched;
        }
    });
  }
  
//  public static void main(String[] args) throws Exception {
//    Class.forName("com.mysql.jdbc.Driver");
//    String url = "jdbc:mysql://localhost:3306/monitor?useUnicode=true&characterEncoding=UTF-8";
//
//    Connection conn = DriverManager.getConnection(url, "monitor", "monitor");
//
//    DatabaseMetaData mtdt = conn.getMetaData();
//
//    ResultSet rs = mtdt.getTables(conn.getCatalog(), "%", "MetricRecord%", new String[]{"TABLE"});
//    
//    while (rs.next()) {
//      System.out.print(rs.getString("TABLE_NAME"));
//    }
//    conn.close();
//  }
}
