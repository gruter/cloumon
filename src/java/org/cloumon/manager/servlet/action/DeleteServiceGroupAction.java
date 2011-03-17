package org.cloumon.manager.servlet.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;


public class DeleteServiceGroupAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String serviceGroupName = request.getParameter("serviceGroupName");
      if(serviceGroupName == null || serviceGroupName.length() == 0) {
        throw new IOException("No serviceGroupName parameter");
      }
      MonitorManagerServer.getMonitorService().removeServiceGroup(serviceGroupName);
      
      return "success";
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    } 
  }

}
