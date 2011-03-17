package org.cloumon.manager.servlet.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;
import org.cloumon.manager.alarm.AlarmManager;
import org.cloumon.thrift.MonitorItem;

public class ModifyMonitorItemAction extends MonitorAction {

  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      MonitorItem monitorItem = new MonitorItem();
      
      monitorItem.setItemId(request.getParameter("itemId"));
      monitorItem.setPeriod(Integer.parseInt(request.getParameter("period")));
      monitorItem.setParams(request.getParameter("params"));
      monitorItem.setDescription(request.getParameter("description"));
      
      String alarmExpr = request.getParameter("alarmExpr");
      String occurTimes = request.getParameter("occurTimes");
      String alarmTo = request.getParameter("alarmTo");
      
      boolean autoDeployAlarm = "Y".equals(request.getParameter("autoDeployAlarm"));
      monitorItem.setAlarm(AlarmManager.getAlaramDBStr(alarmExpr, occurTimes, alarmTo));
      
      MonitorManagerServer.getMonitorService().modifyMonitorItem(monitorItem, autoDeployAlarm);
      
      return "success";
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    }     
  }
}
