package org.cloumon.manager.servlet.action;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;

public class CheckMetricHistoryTimeAction extends MonitorAction {

  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String startTime = request.getParameter("startTime");
      String endTime = request.getParameter("endTime");
      
      return MonitorManagerServer.getMonitorService().checkMetricHistoryTime(startTime, endTime);
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    }     
  }
  public static void main(String[] args) {
    System.out.println(">>>>" + new Date(1299637142000L) + ">" + new Date(1299646807000L));
  }
}
