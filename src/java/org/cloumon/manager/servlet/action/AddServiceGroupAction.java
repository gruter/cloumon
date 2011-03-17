package org.cloumon.manager.servlet.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;
import org.cloumon.thrift.ServiceGroup;

public class AddServiceGroupAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      ServiceGroup serviceGroup = new ServiceGroup();
      serviceGroup.setServiceGroupName(request.getParameter("serviceGroupName"));
      MonitorManagerServer.getMonitorService().addServiceGroup(serviceGroup);
      return "success";
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    } 
  }
}
