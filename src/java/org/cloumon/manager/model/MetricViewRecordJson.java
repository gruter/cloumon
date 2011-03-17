package org.cloumon.manager.model;

import java.util.Date;

import org.cloumon.manager.MonitorServiceImpl;
import org.cloumon.manager.servlet.JsonUtil;
import org.cloumon.thrift.MetricViewRecord;

@SuppressWarnings("serial")
public class MetricViewRecordJson extends MetricViewRecord {
  
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
    sb.append("\"hostIp\":");
    if (this.hostIp == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.hostIp));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"hostName\":");
    if (this.hostName == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.hostName));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"resourceName\":");
    if (this.resourceName == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.resourceName));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"timestamp\":");
    sb.append(JsonUtil.getJsonValue(MonitorServiceImpl.dateFormat.format(new Date(this.timestamp))));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"monitorData\":");
    if (this.monitorData == null) {
      sb.append("null");
    } else {
      try {
        sb.append(JsonUtil.getJsonValue(MonitorServiceImpl.numberFormat.format(Double.parseDouble(new String(this.monitorData.array())))));
      } catch (NumberFormatException e) {
        sb.append(JsonUtil.getJsonValue(new String(this.monitorData.array())));
      }
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"recordId\":");
    if (this.recordId == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.recordId));
    }
    first = false;
    sb.append("}");
    return sb.toString();
  }

}
