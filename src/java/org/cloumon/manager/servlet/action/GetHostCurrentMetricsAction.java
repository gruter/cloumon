package org.cloumon.manager.servlet.action;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;
import org.cloumon.manager.servlet.JsonUtil;
import org.cloumon.thrift.MetricViewRecord;

public class GetHostCurrentMetricsAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String hostName = request.getParameter("hostName");
      if(hostName == null) {
        throw new IOException("No hostName parameter");
      }
      String groupName = request.getParameter("groupName");
      List<MetricViewRecord> records = MonitorManagerServer.getMonitorService().getHostCurrentMetrics(hostName, groupName);
      
      return JsonUtil.listToJson(records);
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    } 
  }
}

