package org.cloumon.manager.servlet.action;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;


public class AddServiceGroupHostAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String hostsParam = request.getParameter("hosts");
      if(hostsParam == null || hostsParam.length() == 0) {
        return "no hosts param";
      }
      String serviceGroupName = request.getParameter("serviceGroupName");
      if(serviceGroupName == null || serviceGroupName.length() == 0) {
        return "no serviceGroupName param";
      }
      
      MonitorManagerServer.getMonitorService().addHostToService(serviceGroupName, Arrays.asList(hostsParam.split(",")));
      return "success";
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    } 
  }
}
