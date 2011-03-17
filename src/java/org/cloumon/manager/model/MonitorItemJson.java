package org.cloumon.manager.model;

import org.cloumon.manager.alarm.AlarmManager;
import org.cloumon.manager.servlet.JsonUtil;
import org.cloumon.thrift.MonitorItem;

@SuppressWarnings("serial")
public class MonitorItemJson extends MonitorItem {
  public boolean equals(MonitorItemJson obj) {
    return this.itemId.equals(obj.getItemId());
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
  
    sb.append("\"itemId\":");
    if (this.itemId == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.itemId));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"itemName\":");
    if (this.itemName == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.itemName));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"groupName\":");
    if (this.groupName == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.groupName));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"defaultItem\":");
    sb.append(JsonUtil.getJsonValue(this.defaultItem));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"adaptorClass\":");
    if (this.adaptorClass == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.adaptorClass));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"period\":");
    sb.append(JsonUtil.getJsonValue(this.period));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"params\":");
    if (this.params == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.params));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"description\":");
    if (this.description == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.description));
    }
    if (this.alarm != null && this.alarm.trim().length() > 0) {
      Alarm alarmObject = AlarmManager.parseAlarm(this.alarm);
      if(alarmObject != null) {
        first = false;
        if (!first) sb.append(", ");
        sb.append(alarmObject.toString());
      }
    }
    first = false;
    sb.append("}");
    return sb.toString();
  }
}
