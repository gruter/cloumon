package org.cloumon.manager.model;

import org.cloumon.manager.servlet.JsonUtil;
import org.cloumon.thrift.SystemMachineInfo;

@SuppressWarnings("serial")
public class SystemMachineInfoJson extends SystemMachineInfo {
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("");
    boolean first = true;

    sb.append("\"machine_name\":");
    if (this.name == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.name));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_version\":");
    if (this.version == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.version));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_arch\":");
    if (this.arch == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.arch));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_machine\":");
    if (this.machine == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.machine));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_description\":");
    if (this.description == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.description));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_patchLevel\":");
    if (this.patchLevel == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.patchLevel));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_vendor\":");
    if (this.vendor == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.vendor));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_vendorVersion\":");
    if (this.vendorVersion == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.vendorVersion));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_vendorName\":");
    if (this.vendorName == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.vendorName));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_vendorCodeName\":");
    if (this.vendorCodeName == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.vendorCodeName));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_dataModel\":");
    if (this.dataModel == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.dataModel));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_cpuEndian\":");
    if (this.cpuEndian == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.cpuEndian));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_jvmVersion\":");
    if (this.jvmVersion == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.jvmVersion));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_jvmVendor\":");
    if (this.jvmVendor == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.jvmVendor));
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("\"machine_jvmHome\":");
    if (this.jvmHome == null) {
      sb.append("null");
    } else {
      sb.append(JsonUtil.getJsonValue(this.jvmHome));
    }
    first = false;
    sb.append("");
    return sb.toString();
  }
}
