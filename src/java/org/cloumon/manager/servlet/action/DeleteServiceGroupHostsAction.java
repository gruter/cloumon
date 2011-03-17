package org.cloumon.manager.servlet.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;


public class DeleteServiceGroupHostsAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String serviceGroupName = request.getParameter("serviceGroupName");
      if(serviceGroupName == null || serviceGroupName.length() == 0) {
        throw new IOException("No serviceGroupName parameter");
      }
      String hostNames = request.getParameter("hostNames");
      if(hostNames == null || hostNames.length() == 0) {
        throw new IOException("No hostNames parameter");
      }
      
      List<String> hostNameList = new ArrayList<String>();
      String[] hostNameTokens = hostNames.split(",");
      for(String eachHost: hostNameTokens) {
        hostNameList.add(eachHost);
      }
      
      MonitorManagerServer.getMonitorService().removeHostsFromServiceGroup(serviceGroupName, hostNameList);
      
      return "success";
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    } 
  }

}
