package org.cloumon.manager.servlet.action.hadoop;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;
import org.cloumon.manager.servlet.JsonUtil;
import org.cloumon.manager.servlet.action.MonitorAction;
import org.cloumon.thrift.HadoopServerStatus;

public class GetDataNodeHostsAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      List<HadoopServerStatus> records = MonitorManagerServer.getMonitorService().getHadoopServerList("hdfs");
      return JsonUtil.listToJson(records);
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    }    
  }
}
