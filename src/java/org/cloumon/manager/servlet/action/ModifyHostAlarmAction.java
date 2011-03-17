package org.cloumon.manager.servlet.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;

public class ModifyHostAlarmAction extends MonitorAction {

  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String hostName = request.getParameter("hostName");
      String hostAlarm = request.getParameter("hostAlarm");
      boolean alarmOn = "true".equals(request.getParameter("alarmOn"));

      if(hostName == null || hostName.length() == 0) {
        throw new IOException("No hostName parameter");
      }
      MonitorManagerServer.getMonitorService().updateAgentAlarm(hostName, hostAlarm, alarmOn);
      
      return "success";
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    }     
  }
}
