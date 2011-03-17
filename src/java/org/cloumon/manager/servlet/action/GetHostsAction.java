package org.cloumon.manager.servlet.action;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;
import org.cloumon.manager.servlet.JsonUtil;
import org.cloumon.thrift.HostInfo;

public class GetHostsAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      List<HostInfo> hostInfos = MonitorManagerServer.getMonitorService().findAllHosts();
      
      return JsonUtil.listToJson(hostInfos);
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    } 
  }

}
