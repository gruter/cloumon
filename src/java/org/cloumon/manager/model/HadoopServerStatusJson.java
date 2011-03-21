package org.cloumon.manager.model;

import org.cloumon.manager.servlet.JsonUtil;
import org.cloumon.thrift.HadoopServerStatus;

@SuppressWarnings("serial")
public class HadoopServerStatusJson extends HadoopServerStatus {
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    sb.append("\"hostName\":");
    if (this.hostName == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.hostName));
    }
    sb.append(", ");
    sb.append("\"nodeType\":");
    if (this.nodeType == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.nodeType));
    }
    sb.append(", ");
    sb.append("\"live\":");
    sb.append(JsonUtil.getJsonValue(this.live ? "live" : "<font color=red>dead</font>"));
    
    sb.append("}");
    return sb.toString();
  }
}
