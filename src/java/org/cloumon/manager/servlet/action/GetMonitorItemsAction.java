package org.cloumon.manager.servlet.action;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;
import org.cloumon.manager.servlet.JsonUtil;
import org.cloumon.thrift.MonitorItem;

public class GetMonitorItemsAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      List<MonitorItem> items = MonitorManagerServer.getMonitorService().findAllMonitorItems();
      
      return JsonUtil.listToJson(items);
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    } 
  }

}
