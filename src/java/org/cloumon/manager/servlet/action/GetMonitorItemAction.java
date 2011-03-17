package org.cloumon.manager.servlet.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;
import org.cloumon.thrift.MonitorItem;

public class GetMonitorItemAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String itemId = request.getParameter("itemId");
      if(itemId == null || itemId.length() == 0) {
        throw new IOException("No itemId parameter");
      }
      MonitorItem item = MonitorManagerServer.getMonitorService().getMonitorItem(itemId);
      
      return "[" + item.toString() + "]";
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    } 
  }

}
