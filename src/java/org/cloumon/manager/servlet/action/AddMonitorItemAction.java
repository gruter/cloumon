package org.cloumon.manager.servlet.action;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;
import org.cloumon.manager.alarm.AlarmManager;
import org.cloumon.thrift.MonitorItem;

public class AddMonitorItemAction extends MonitorAction {

  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      MonitorItem monitorItem = new MonitorItem();
      
      monitorItem.setItemName(request.getParameter("itemName"));
      monitorItem.setGroupName(request.getParameter("itemGroup"));
      monitorItem.setAdaptorClass(request.getParameter("adaptor"));
      monitorItem.setDefaultItem("true".equals(request.getParameter("defaultItem")));
      monitorItem.setPeriod(Integer.parseInt(request.getParameter("period")));
      monitorItem.setParams(request.getParameter("params"));
      monitorItem.setDescription(request.getParameter("description"));
      
      String alarmExpr = request.getParameter("alarmExpr");
      String occurTimes = request.getParameter("occurTimes");
      String alarmTo = request.getParameter("alarmTo");
      
      monitorItem.setAlarm(AlarmManager.getAlaramDBStr(alarmExpr, occurTimes, alarmTo));
      
      MonitorManagerServer.getMonitorService().addMinitorItem(Arrays.asList(new MonitorItem[]{monitorItem}));
      
      return "success";
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    }     
  }
}
