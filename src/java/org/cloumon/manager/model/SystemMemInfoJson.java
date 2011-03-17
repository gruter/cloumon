package org.cloumon.manager.model;

import org.cloumon.manager.servlet.JsonUtil;
import org.cloumon.thrift.SystemMemInfo;

@SuppressWarnings("serial")
public class SystemMemInfoJson extends SystemMemInfo {
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("");
    boolean first = true;

    sb.append("\"meminfo_memory\":");
    sb.append(this.memory);
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"meminfo_total\":");
    sb.append(JsonUtil.getJsonValue(this.total));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"meminfo_used\":");
    sb.append(JsonUtil.getJsonValue(this.used));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"meminfo_free\":");
    sb.append(JsonUtil.getJsonValue(this.free));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"meminfo_actualUsed\":");
    sb.append(JsonUtil.getJsonValue(this.actualUsed));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"meminfo_actualFree\":");
    sb.append(JsonUtil.getJsonValue(this.actualFree));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"meminfo_swapTotal\":");
    sb.append(JsonUtil.getJsonValue(this.swapTotal));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"meminfo_swapUsed\":");
    sb.append(JsonUtil.getJsonValue(this.swapUsed));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"meminfo_swapFree\":");
    sb.append(JsonUtil.getJsonValue(this.swapFree));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"meminfo_swapPageIn\":");
    sb.append(JsonUtil.getJsonValue(this.swapPageIn));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"meminfo_swapPageOut\":");
    sb.append(JsonUtil.getJsonValue(this.swapPageOut));
    first = false;
    sb.append("");
    return sb.toString();
  }
}
