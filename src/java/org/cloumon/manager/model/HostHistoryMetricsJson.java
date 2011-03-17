package org.cloumon.manager.model;

import org.cloumon.manager.MonitorServiceImpl;
import org.cloumon.thrift.HostHistoryMetrics;
import org.cloumon.thrift.HostHistoryMetricsItem;

public class HostHistoryMetricsJson extends HostHistoryMetrics {
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("{");
    sb.append("\"logTime\":\"").append(logTime).append("\"");
    
    for(HostHistoryMetricsItem eachItem: datas) {
      try {
        sb.append(",\"").append(eachItem.getItemName()).append("\":\"");
        sb.append(MonitorServiceImpl.numberFormat.format(Double.parseDouble(eachItem.getMonitorData()))).append("\"");
      } catch (NumberFormatException e) {
        sb.append(eachItem.getMonitorData()).append("\"");
      }
    }
    sb.append("}");
    
    return sb.toString();
  }
}
