package org.cloumon.manager.servlet.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;

public class DeleteMonitorItemAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String itemId = request.getParameter("itemId");
      if(itemId == null || itemId.length() == 0) {
        throw new IOException("No itemId parameter");
      }
      MonitorManagerServer.getMonitorService().removeMonitorItem(itemId);
      
      return "success";
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    } 
  }

}
