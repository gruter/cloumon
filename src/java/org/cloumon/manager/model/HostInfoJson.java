package org.cloumon.manager.model;

import org.cloumon.common.util.CollectionUtils;
import org.cloumon.manager.alarm.AlarmManager;
import org.cloumon.manager.servlet.JsonUtil;
import org.cloumon.thrift.HostInfo;

@SuppressWarnings("serial")
public class HostInfoJson extends HostInfo {
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    sb.append("\"hostName\":");
    if (this.hostName == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.hostName));
    }
    sb.append(", ");
    sb.append("\"hostType\":");
    if (this.hostType == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.hostType));
    }
    sb.append(", ");
    sb.append("\"hostIps\":");
    if (this.hostIps == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(CollectionUtils.toString(this.hostIps)));
    }
    sb.append(", ");
    if (this.cpuInfo == null) {
      sb.append("\"cpuInfo\":");
      sb.append("null");
    } else {
      sb.append(this.cpuInfo.toString());
    }
    sb.append(", ");
    sb.append("\"fileSystemInfo\":");
    if (this.fileSystemInfo == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(CollectionUtils.toString(this.fileSystemInfo.getFileSystems())));
    }
    sb.append(", ");
    if (this.machineInfo == null) {
      sb.append("\"machineInfo\":");
      sb.append("null");
    } else {
      sb.append(this.machineInfo.toString());
    }
    sb.append(", ");
    if (this.memInfo == null) {
      sb.append("\"memInfo\":");
      sb.append("null");
    } else {
      sb.append(this.memInfo.toString());
    }
    sb.append(", ");
    sb.append("\"networkInfo\":");
    if (this.networkInfo == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(CollectionUtils.toString(this.networkInfo.getNetworkInterfaces())));
    }
    sb.append(", ");
    sb.append("\"liveStatus\":");
    sb.append(JsonUtil.getJsonValue(this.liveStatus ? "live" : "<font color=red>dead</font>"));
    
    sb.append(", ");
    sb.append("\"alarmOn\":");
    sb.append(JsonUtil.getJsonValue(this.alarmOn ? "true" : "false"));

    sb.append(", ");
    sb.append("\"hostAlarm\":");
    sb.append(JsonUtil.getJsonValue(this.hostAlarm));
    
    boolean first = false;
    if (this.alarm != null && this.alarm.trim().length() > 0) {
      Alarm alarmObject = AlarmManager.parseAlarm(this.alarm);
      if(alarmObject != null) {
        first = false;
        if (!first) sb.append(", ");
        sb.append(alarmObject.toString());
      }
    }    
    sb.append("}");
    return sb.toString();
  }
}
