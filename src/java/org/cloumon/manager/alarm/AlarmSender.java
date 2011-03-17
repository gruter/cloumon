package org.cloumon.manager.alarm;

import org.cloumon.manager.model.Alarm;
import org.cloumon.thrift.HostInfo;
import org.cloumon.thrift.MonitorItem;

import com.gruter.common.conf.GruterConf;

public interface AlarmSender {
  public void sendAgentFailAlarm(GruterConf conf, HostInfo hostInfo);
  public void sendAlarm(GruterConf conf, Alarm alarm, HostInfo hostInfo, MonitorItem monitorItem, String alarmExpr, byte[] monitorData);
}
