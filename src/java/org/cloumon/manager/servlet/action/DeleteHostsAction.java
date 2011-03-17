package org.cloumon.manager.servlet.action;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;


public class DeleteHostsAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String hostNames = request.getParameter("hostNames");
      if(hostNames == null || hostNames.length() == 0) {
        throw new IOException("No hostNames parameter");
      }
      List<String> hostNameList = Arrays.asList(hostNames.split(","));
      MonitorManagerServer.getMonitorService().removeHosts(hostNameList);
      
      return "success";
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    } 
  }

}
