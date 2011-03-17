package org.cloumon.manager.servlet.action;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;
import org.cloumon.manager.MonitorServiceImpl;
import org.cloumon.thrift.HostSummaryMetrics;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetHostSummaryMetricsAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      List<HostSummaryMetrics> hostSummaryMetrics = MonitorManagerServer.getMonitorService().getHostSummaryMetrics();
      
      JSONArray jsonArray = new JSONArray();
      for (HostSummaryMetrics eachMetrics: hostSummaryMetrics) {
        JSONObject metricsObject = new JSONObject();
        metricsObject.put("hostName", eachMetrics.getHostName());
        metricsObject.put("hostIp", eachMetrics.getHostIp());
        metricsObject.put("cpuLoad", format(eachMetrics.getCpuLoad()));
        metricsObject.put("cpuUser", format(eachMetrics.getCpuUser()));
        metricsObject.put("diskUsed", format(eachMetrics.getDiskUsed()));
        metricsObject.put("netIn", format(eachMetrics.getNetIn()));
        metricsObject.put("netOut", format(eachMetrics.getNetOut()));
        metricsObject.put("logTime", format(eachMetrics.getLogTime()));
        metricsObject.put("liveStatus", eachMetrics.isLiveStatus() ? "live": "<font color=red>dead</font>");

        jsonArray.put(metricsObject);
      }
      return jsonArray.toString();
    } catch (JSONException e) {
      e.printStackTrace();
      return "";
    } catch (TException e) {
      e.printStackTrace();
      throw new IOException(e.getMessage(), e);
    }
  }
  
  private String format(String value) {
    if(value == null) {
      return "";
    }
    try {
      return MonitorServiceImpl.numberFormat.format(Double.parseDouble(value));
    } catch (NumberFormatException e) {
      return value;
    }
  }
}

