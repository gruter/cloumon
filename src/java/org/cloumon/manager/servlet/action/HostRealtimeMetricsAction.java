package org.cloumon.manager.servlet.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cloumon.manager.servlet.MonitorControllerServlet;

public class HostRealtimeMetricsAction extends MonitorAction {

  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String host = request.getParameter("host");
      HttpClient client = new HttpClient();
      int port = MonitorControllerServlet.conf.getInt("cloumon.agent.httpPort", 8124);
      GetMethod method = new GetMethod("http://" + host + ":" + port + "/agent?action=GetMetrics");
      int result = client.executeMethod(method);
      return method.getResponseBodyAsString();
    } catch(Exception e) {
      throw new IOException(e.getMessage(), e);
    }
  }
}
