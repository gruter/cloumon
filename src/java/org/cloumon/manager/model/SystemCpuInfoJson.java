package org.cloumon.manager.model;

import org.cloumon.manager.servlet.JsonUtil;
import org.cloumon.thrift.SystemCpuInfo;

@SuppressWarnings("serial")
public class SystemCpuInfoJson extends SystemCpuInfo {
  @Override
  public String toString() {
    //StringBuilder sb = new StringBuilder("{");
    StringBuilder sb = new StringBuilder("");
    boolean first = true;

    sb.append("\"cpuinfo_vendor\":");
    if (this.vendor == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.vendor));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"cpuinfo_model\":");
    if (this.model == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.model));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"cpuinfo_mhz\":");
    sb.append(JsonUtil.getJsonValue(this.mhz));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"cpuinfo_cacheSize\":");
    sb.append(JsonUtil.getJsonValue(this.cacheSize));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"cpuinfo_totalCores\":");
    sb.append(JsonUtil.getJsonValue(this.totalCores));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"cpuinfo_totalSockets\":");
    sb.append(JsonUtil.getJsonValue(this.totalSockets));
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"cpuinfo_coresPerSocket\":");
    sb.append(JsonUtil.getJsonValue(this.coresPerSocket));
    first = false;
    sb.append("");
    return sb.toString();
  }
}
