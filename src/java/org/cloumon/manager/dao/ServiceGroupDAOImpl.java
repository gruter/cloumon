package org.cloumon.manager.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cloumon.thrift.ServiceGroup;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("serviceGroupDAO")
public class ServiceGroupDAOImpl extends MonitorJdbcDaoSupport implements ServiceGroupDAO {
  static final String insertSql = "INSERT INTO ServiceGroup ( ServiceGroupName ) VALUES ( ? )";
  static final String selectAllSql = "SELECT ServiceGroupName FROM ServiceGroup";
  static final String deleteSql = "DELETE FROM ServiceGroup WHERE ServiceGroupName = ?";
  
  static final String insertHostSql = "INSERT INTO ServiceHosts ( ServiceGroupName, HostName ) VALUES ( ?, ? )";
  static final String deleteHostSql = "DELETE FROM ServiceHosts WHERE ServiceGroupName = ? AND HostName = ?";
  static final String deleteServiceHostsSql = "DELETE FROM ServiceHosts WHERE ServiceGroupName = ?";
  
  static final String deleteAllSql = "DELETE FROM ServiceGroup";
  
  static final String deleteAllServiceHostSql = "DELETE FROM ServiceHosts";
  
  
  @Override
  public void addServiceGroup(ServiceGroup serviceGroup) throws IOException {
    getJdbcTemplate().update(insertSql, serviceGroup.getServiceGroupName());
  }

  @Override
  public List<ServiceGroup> findAllServiceGroups() throws IOException {
    return getJdbcTemplate().query(selectAllSql, new ServiceGroupRowMapper());
  }

  @Override
  public void removeServiceGroup(String serviceGroupName) throws IOException {
    getJdbcTemplate().update(deleteSql, serviceGroupName);
  }
  
  public void addHostToServiceGroup(String serviceGroupName, String hostName) throws IOException {
    getJdbcTemplate().update(insertHostSql, serviceGroupName, hostName);
  }
  
  public void removeHostToServiceGroup(String serviceGroupName, String hostName) throws IOException {
    getJdbcTemplate().update(deleteHostSql, serviceGroupName, hostName);
  }
  
  class ServiceGroupRowMapper implements RowMapper<ServiceGroup> {
    @Override
    public ServiceGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
      ServiceGroup serviceGroup = new ServiceGroup();
      serviceGroup.setServiceGroupName(rs.getString(1));
      return serviceGroup;
    }
  }

  @Override
  public void removeServiceHosts(String serviceGroupName) throws IOException {
    getJdbcTemplate().update(deleteServiceHostsSql, serviceGroupName);
  }
  
  @Override
  public void deleteAll() throws IOException {
    getJdbcTemplate().execute(deleteAllSql);
    getJdbcTemplate().execute(deleteAllServiceHostSql);
  }
}
