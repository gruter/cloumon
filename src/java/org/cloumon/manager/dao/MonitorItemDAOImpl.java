package org.cloumon.manager.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cloumon.manager.alarm.AlarmManager;
import org.cloumon.manager.model.Alarm;
import org.cloumon.manager.model.MonitorItemJson;
import org.cloumon.thrift.MonitorItem;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("monitorItemDAO")
public class MonitorItemDAOImpl extends MonitorJdbcDaoSupport implements MonitorItemDAO {
  private static final String columns = " a.ItemID, a.ItemName, a.GroupName, a.AdaptorClass, " +
                                        " a.DefaultItem, a.Period, a.Params, a.Description, a.Alarm ";
  
  private static final String selectAllSql = "SELECT " + columns + " FROM MonitorItem a";
  
  private static final String selectByIdSql = "SELECT " + columns + " FROM MonitorItem a WHERE a.ItemId = ?";
  
  private static final String selectDefaultSql = "SELECT " + columns + " FROM MonitorItem a WHERE a.DefaultItem = 'Y'";
  
  private static final String selectByGroupSql = "SELECT " + columns + " FROM MonitorItem a WHERE a.GroupName = ?";
  
  private static final String selectByHostSql = "SELECT " + columns + " FROM MonitorItem a, HostMonitorItem b \n" +
                                                " WHERE a.ItemId = b.ItemId AND b.HostName = ?";
  
  private static final String selectByHostGroupSql = "SELECT " + columns + " FROM MonitorItem a, HostMonitorItem b \n" +
                                                " WHERE a.ItemId = b.ItemId AND b.HostName = ? AND a.GroupName = ?";

  private static final String insertSql = "INSERT INTO MonitorItem ( " +
                                           "      ItemID, ItemName, GroupName, AdaptorClass, " +
                                           "      DefaultItem, Period, Params, Description, Alarm) " +  		
                                           "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
  
  private static final String updateSql = "UPDATE MonitorItem SET " +
                                          //"       ItemName = ?, GroupName = ?, AdaptorClass = ?, DefaultItem = ?, \" +" +
                                          "       Period = ?, Params = ?, Description = ?, Alarm =? " +
                                          " WHERE ItemId = ?";

  private static final String updateHostAlarmSql = "UPDATE HostMonitorItem SET  Alarm = ? WHERE HostName = ?";
  
  static final String deleteSql = "DELETE FROM MonitorItem WHERE ItemId = ?";
  
  static final String insertHostSql = "INSERT INTO HostMonitorItem ( ItemID, HostName, Alarm ) VALUES ( ?, ?, ? )";
  
  static final String deleteHostSql = "DELETE FROM HostMonitorItem WHERE ItemID = ? AND HostName = ?";
  
  static final String deleteByHostSql = "DELETE FROM HostMonitorItem WHERE HostName = ?";
  
  static final String deleteHostMonitorItemSql = "DELETE FROM HostMonitorItem WHERE ItemID = ?";
  
  static final String updateHostMonitorItemSql = "UPDATE HostMonitorItem SET Alarm = ? WHERE ItemID = ? AND HostName = ? ";

  static final String selectAlarmSql = "SELECT ItemId, HostName, Alarm FROM HostMonitorItem";
  
  static final String deleteAllSql = "DELETE FROM MonitorItem";
  
  @Override
  public void addHostToMonitorItem(String itemId, String hostName, String alarm) throws IOException {
    getJdbcTemplate().update(insertHostSql, itemId, hostName, alarm);
  }
  
  @Override
  public void removeHostFromMonitorItem(String itemId, String hostName) throws IOException {
    getJdbcTemplate().update(deleteHostSql, itemId, hostName);
  }
  
  @Override
  public List<MonitorItem> findAllMonitorItems() throws IOException {
    return getJdbcTemplate().query(selectAllSql, new MonitorItemRowMapper());
  }

  @Override
  public List<MonitorItem> findHostMonitorItems(String hostName) throws IOException {
    return getJdbcTemplate().query(selectByHostSql, new MonitorItemRowMapper(), hostName);
  }


  @Override
  public List<MonitorItem> findDefaultMonitorItems() throws IOException {
    return getJdbcTemplate().query(selectDefaultSql, new MonitorItemRowMapper());
  }
  
  @Override
  public List<MonitorItem> findHostMonitorItemsByGroup(String hostName, String groupName)  throws IOException {
    return getJdbcTemplate().query(selectByHostGroupSql, new MonitorItemRowMapper(), hostName, groupName);
  }
  
  @Override
  public List<MonitorItem> findMonitorItemByGroup(String groupName)  throws IOException {
    return getJdbcTemplate().query(selectByGroupSql, new MonitorItemRowMapper(), groupName);
  }
  
  @Override
  public void addMonitorItem(MonitorItem monitorItem) throws IOException {
    getJdbcTemplate().update(insertSql, 
        monitorItem.getItemId(),
        monitorItem.getItemName(),
        monitorItem.getGroupName(),
        monitorItem.getAdaptorClass(),
        monitorItem.isDefaultItem() ? "Y": "N",
        monitorItem.getPeriod(),
        monitorItem.getParams(),
        monitorItem.getDescription(),
        monitorItem.getAlarm());
  }
  @Override
  public void updateMonitorItem(MonitorItem monitorItem) throws IOException {
    getJdbcTemplate().update(updateSql, 
//        monitorItem.getItemName(),
//        monitorItem.getGroupName(),
//        monitorItem.getAdaptorClass(),
//        monitorItem.isDefaultItem() ? "Y": "N",
        monitorItem.getPeriod(),
        monitorItem.getParams(),
        monitorItem.getDescription(),
        monitorItem.getAlarm(),
        monitorItem.getItemId());
  }
  
  class MonitorItemRowMapper implements RowMapper<MonitorItem> {
    @Override
    public MonitorItem mapRow(ResultSet rs, int rowNum) throws SQLException {
      MonitorItem item = new MonitorItemJson();
      
      int index = 1;
      item.setItemId(rs.getString(index++));
      item.setItemName(rs.getString(index++));
      item.setGroupName(rs.getString(index++));
      item.setAdaptorClass(rs.getString(index++));
      item.setDefaultItem("Y".equals(rs.getString(index++)));
      item.setPeriod(rs.getInt(index++));
      item.setParams(rs.getString(index++));
      item.setDescription(rs.getString(index++));
      item.setAlarm(rs.getString(index++));

      return item;
    }
  }

  @Override
  public void deleteMonitorItem(String monitorItemId) throws IOException {
    getJdbcTemplate().update(deleteSql, monitorItemId);
  }
  
  @Override
  public void deleteHostMonitorItem(String monitorItemId) throws IOException {
    getJdbcTemplate().update(deleteHostMonitorItemSql, monitorItemId);
  }

  @Override
  public MonitorItem findMonitorItemById(String itemId) throws IOException {
    List<MonitorItem> items = getJdbcTemplate().query(selectByIdSql, new MonitorItemRowMapper(), itemId);
    
    if(items != null && items.size() > 0) {
      return items.get(0);
    } else {
      return null;
    }
  }

  @Override
  public void updateHostMonitorItem(String monitorItemId, String hostName, String alarm) throws IOException {
    getJdbcTemplate().update(updateHostMonitorItemSql, alarm, monitorItemId, hostName);
  }

  @Override
  public List<Alarm> findAllHostAlarms() throws IOException {
    return getJdbcTemplate().query(selectAlarmSql, new RowMapper<Alarm>() {
      @Override
      public Alarm mapRow(ResultSet rs, int rowNum) throws SQLException {
        Alarm alarm = AlarmManager.parseAlarm(rs.getString(3));
        alarm.setItemId(rs.getString(1));
        alarm.setHostName(rs.getString(2));
        return alarm;
      }
    });
  }
  
  @Override
  public void deleteAll() throws IOException {
    getJdbcTemplate().execute(deleteAllSql);
  }
  
  public void updateAlarmByHostName(String agentHostName, String alarm) throws IOException {
    getJdbcTemplate().update(updateHostAlarmSql, alarm, agentHostName);
  }

  @Override
  public void deleteHostItems(String hostName) throws IOException {
    getJdbcTemplate().update(deleteByHostSql, hostName);
  }
}
