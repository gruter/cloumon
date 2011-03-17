package org.cloumon.manager.servlet.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;


public class AddMonitorItemToHostAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String hostsParam = request.getParameter("hosts");
      List<String> hosts;
      if(hostsParam == null || hostsParam.length() == 0) {
        hosts = new ArrayList<String>();
      } else {
        hosts = Arrays.asList(hostsParam.split(","));
      }
      String itemId = request.getParameter("itemId");
      if(itemId == null || itemId.length() == 0) {
        return "no itemId param";
      }
      
      MonitorManagerServer.getMonitorService().addHostToMonitorItem(Arrays.asList(new String[]{itemId}), hosts);
      return "success";
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    } 
  }
}
