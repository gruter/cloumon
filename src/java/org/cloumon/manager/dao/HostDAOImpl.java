package org.cloumon.manager.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cloumon.common.util.CollectionUtils;
import org.cloumon.manager.model.HostInfoJson;
import org.cloumon.manager.model.SystemCpuInfoJson;
import org.cloumon.manager.model.SystemMachineInfoJson;
import org.cloumon.manager.model.SystemMemInfoJson;
import org.cloumon.thrift.HostInfo;
import org.cloumon.thrift.SystemCpuInfo;
import org.cloumon.thrift.SystemFileSystemInfo;
import org.cloumon.thrift.SystemMachineInfo;
import org.cloumon.thrift.SystemMemInfo;
import org.cloumon.thrift.SystemNetworkInterfaceInfo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("hostDAO")
public class HostDAOImpl extends MonitorJdbcDaoSupport implements HostDAO {
  static final String columns = 
    "a.HostIP, a.HostName, a.HostType, a.CpuCores, a.CpuModel, " +
    "a.CpuVendor, a.CpuMhz, a.MachineName, a.MachineVersion, a.MachineArch, " +
    "a.Machine, a.MachineDescription, a.MachinePatchLevel, a.MachineVendor, a.MachineVendorVersion, " + 
    "a.MachineVendorName, a.MachineVendorCodeName, a.MachineDataModel, a.MachineCpuEndian, a.MachineJvmVersion, " +
    "a.MachineJvmVendor, a.MachineJvmHome, MemotyTotal, a.SwapTotal, a.FileSystem, " +
    "a.NetworkInterfaces, a.LiveStatus, a.AlarmOn, a.HostAlarm";

  static final String insertSql = 
    "INSERT INTO Host (" + columns.replace("a.", "") + " )" + 
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
    "        ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
    "        ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  static final String selectAllSql = 
    "SELECT " + columns + " , ' ' as Alarm FROM Host a ORDER BY a.HostName";
  
  static final String selectByNameSql = 
    "SELECT " + columns + " , ' ' as Alarm FROM Host a WHERE a.HostName = ?";
  
  static final String selectByServiceSql = 
    "SELECT " + columns + " , ' ' as Alarm FROM Host a, ServiceHosts b WHERE a.HostName = b.HostName AND b.ServiceGroupName = ? ORDER BY a.HostName";
  
  static final String selectByMonitorItemSql = 
    "SELECT " + columns + ", b.Alarm FROM Host a, HostMonitorItem b WHERE a.HostName = b.HostName AND b.ItemId = ? ORDER BY a.HostName";
  
  static final String deleteAllHostSql = "DELETE FROM Host";
  
  static final String deleteHostSql = "DELETE FROM Host WHERE HostName = ?";
  
  static final String deleteAllHostMonitorItemSql = "DELETE FROM HostMonitorItem";
  
  static final String updateLiveStatusSql = "UPDATE Host Set LiveStatus = ? WHERE HostName = ?";
  
  static final String updateAlarmSql = "UPDATE Host Set HostAlarm = ?, AlarmOn = ? WHERE HostName = ?";
  
  static final String updateAlarmOnSql = "UPDATE Host Set AlarmOn = ? WHERE HostName = ?";
  
  @Override
  public void addHost(HostInfo hostInfo) throws IOException {
    getJdbcTemplate().update(insertSql, 
        CollectionUtils.toString(hostInfo.getHostIps()),
        hostInfo.getHostName(),
        hostInfo.getHostType(),
        hostInfo.getCpuInfo().getTotalCores(),
        hostInfo.getCpuInfo().getModel(),
        hostInfo.getCpuInfo().getVendor(),
        hostInfo.getCpuInfo().getMhz(),
        hostInfo.getMachineInfo().getName(),
        hostInfo.getMachineInfo().getVersion(),
        hostInfo.getMachineInfo().getArch(),
        hostInfo.getMachineInfo().getMachine(),
        hostInfo.getMachineInfo().getDescription(),
        hostInfo.getMachineInfo().getPatchLevel(),
        hostInfo.getMachineInfo().getVendor(),
        hostInfo.getMachineInfo().getVendorVersion(),
        hostInfo.getMachineInfo().getVendorName(),
        hostInfo.getMachineInfo().getVendorCodeName(),
        hostInfo.getMachineInfo().getDataModel(),
        hostInfo.getMachineInfo().getCpuEndian(),
        hostInfo.getMachineInfo().getJvmVersion(),
        hostInfo.getMachineInfo().getJvmVendor(),
        hostInfo.getMachineInfo().getJvmHome(),
        hostInfo.getMemInfo().getMemory(),
        hostInfo.getMemInfo().getSwapTotal(),
        CollectionUtils.toString(hostInfo.getFileSystemInfo().getFileSystems()),
        CollectionUtils.toString(hostInfo.getNetworkInfo().getNetworkInterfaces()),
        (hostInfo.isLiveStatus() ? "Y" : "N"),
        (hostInfo.isAlarmOn() ? "Y" : "N"),
        hostInfo.getHostAlarm()
        );
  }

  class HostRowMapper implements RowMapper<HostInfo> {
    @Override
    public HostInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
      HostInfo host = new HostInfoJson();

      int index = 1;
      host.setHostIps(CollectionUtils.toList(rs.getString(index++)));
      host.setHostName(rs.getString(index++));
      host.setHostType(rs.getString(index++));
      
      SystemCpuInfo cpuInfo = new SystemCpuInfoJson();
      cpuInfo.setTotalCores(rs.getInt(index++));
      cpuInfo.setModel(rs.getString(index++));
      cpuInfo.setVendor(rs.getString(index++));
      cpuInfo.setMhz(rs.getInt(index++));
      
      SystemMachineInfo machineInfo = new SystemMachineInfoJson();
      machineInfo.setName(rs.getString(index++));
      machineInfo.setVersion(rs.getString(index++));
      machineInfo.setArch(rs.getString(index++));
      machineInfo.setMachine(rs.getString(index++));
      machineInfo.setDescription(rs.getString(index++));
      machineInfo.setPatchLevel(rs.getString(index++));
      machineInfo.setVendor(rs.getString(index++));
      machineInfo.setVendorVersion(rs.getString(index++));
      machineInfo.setVendorName(rs.getString(index++));
      machineInfo.setVendorCodeName(rs.getString(index++));
      machineInfo.setDataModel(rs.getString(index++));
      machineInfo.setCpuEndian(rs.getString(index++));
      machineInfo.setJvmVersion(rs.getString(index++));
      machineInfo.setJvmVendor(rs.getString(index++));
      machineInfo.setJvmHome(rs.getString(index++));

      SystemMemInfo memInfo = new SystemMemInfoJson();
      memInfo.setMemory(rs.getLong(index++));
      memInfo.setSwapTotal(rs.getLong(index++));

      SystemFileSystemInfo fileSystemInfo = new SystemFileSystemInfo();
      fileSystemInfo.setFileSystems(CollectionUtils.toList(rs.getString(index++)));

      SystemNetworkInterfaceInfo networkInfo = new SystemNetworkInterfaceInfo();
      networkInfo.setNetworkInterfaces(CollectionUtils.toList(rs.getString(index++)));

      host.setCpuInfo(cpuInfo);
      host.setMachineInfo(machineInfo);
      host.setMemInfo(memInfo);
      host.setFileSystemInfo(fileSystemInfo);
      host.setNetworkInfo(networkInfo);

      host.setLiveStatus("Y".equals(rs.getString(index++)));
      host.setAlarmOn("Y".equals(rs.getString(index++)));
      host.setHostAlarm(rs.getString(index++));
      host.setAlarm(rs.getString(index++));
      return host;
    }
  }

  @Override
  public List<HostInfo> findAllHosts() throws IOException {
    return getJdbcTemplate().query(selectAllSql, new HostRowMapper());
  }

  @Override
  public HostInfo findHostByName(String hostName) throws IOException {
    List<HostInfo> hostInfos = getJdbcTemplate().query(selectByNameSql, new HostRowMapper(), hostName);
    if(hostInfos.isEmpty()) {
      return null;
    } else {
      return hostInfos.get(0);
    }
  }

  @Override
  public void removeHost(String hostName) throws IOException {
  }

  public List<HostInfo> findHostByServiceGroup(String serviceGroup) throws IOException {
    return getJdbcTemplate().query(selectByServiceSql, new String[]{serviceGroup}, new HostRowMapper());
  }

  @Override
  public List<HostInfo> findHostByMonitorItem(String monitorItemId) throws IOException {
    return getJdbcTemplate().query(selectByMonitorItemSql, new String[]{monitorItemId}, new HostRowMapper());
  }

  @Override
  public void deleteAll() throws IOException {
    getJdbcTemplate().execute(deleteAllHostSql);
    getJdbcTemplate().execute(deleteAllHostMonitorItemSql);
  }
  
  @Override
  public void updateAgentLiveStatus(String agentHostName, boolean status) throws IOException {
    getJdbcTemplate().update(updateLiveStatusSql, (status ? "Y": "N"), agentHostName);
  }
  
  @Override
  public void updateAgentAlarm(String agentHostName, String alarm, boolean on) throws IOException {
    getJdbcTemplate().update(updateAlarmSql, alarm, (on ? "Y": "N"), agentHostName);
  }
  
  @Override
  public void updateAgentAlarm(String agentHostName, boolean on) throws IOException {
    getJdbcTemplate().update(updateAlarmOnSql, (on ? "Y": "N"), agentHostName);
  }

  @Override
  public void deleteHost(String hostName) throws IOException {
    getJdbcTemplate().update(deleteHostSql, hostName);
  }
}
