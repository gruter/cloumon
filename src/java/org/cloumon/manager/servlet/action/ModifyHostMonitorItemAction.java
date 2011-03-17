package org.cloumon.manager.servlet.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;
import org.cloumon.manager.alarm.AlarmManager;


public class ModifyHostMonitorItemAction extends MonitorAction {

  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String alarmExpr = request.getParameter("alarmExpr");
      String occurTimes = request.getParameter("occurTimes");
      String alarmTo = request.getParameter("alarmTo");
      
      MonitorManagerServer.getMonitorService().modifyHostMonitorItem(request.getParameter("itemId"), 
          request.getParameter("hostName"), 
          AlarmManager.getAlaramDBStr(alarmExpr, occurTimes, alarmTo));
      
      return "success";
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    }     
  }
}
