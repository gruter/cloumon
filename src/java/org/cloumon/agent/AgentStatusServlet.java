package org.cloumon.agent;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloumon.agent.item.server.CpuPercMetrics;
import org.cloumon.agent.item.server.MemoryMetrics;
import org.cloumon.agent.item.server.NetworkMetrics;
import org.cloumon.agent.item.server.ServerMetricsManager;
import org.cloumon.thrift.MonitorItem;

@SuppressWarnings("serial")
public class AgentStatusServlet extends HttpServlet {
  private static final Log LOG = LogFactory.getLog(AgentStatusServlet.class);
  ServerMetricsManager serverMetricsManager = new ServerMetricsManager();
  
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    response.setContentType("text/html;charset=UTF-8");
    
    String action = request.getParameter("action");
    PrintWriter out = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"), true);//response.getWriter();
    try {
      if("GetMetrics".equals(action)) {
        String result = getMetrics(request); 
        LOG.info(result);
        out.write(result);
      } else if("GetMonitorItem".equals(action)) {
        out.write(getMonitorItems(request));
      } 
    } catch (Exception e) {
      out.write("error");
    } finally {
      if(out != null) {
        out.close();
      }
    }
  }
  
  private String getMetrics(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    boolean first = true;
    try {
      CpuPercMetrics metrics = serverMetricsManager.getCpuPercMetrics();

      sb.append("{\"cpu\":[\"").append(metrics.getLoad()).append("\",\"");
      sb.append(metrics.getUser()).append("\",\""); 
      sb.append(metrics.getSys()).append("\",\""); 
      sb.append(metrics.getWait()).append("\",\""); 
      sb.append(metrics.getIdle()).append("\"]}");
      first = false;
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
    try {
      if(!first) {
        sb.append(",");
      }
      sb.append("{\"memory\":[\"");
      MemoryMetrics memoryMetrics = serverMetricsManager.getMemoryMetrics();
      sb.append(memoryMetrics.getTotal()).append("\",\""); 
      sb.append(memoryMetrics.getActualUsed()).append("\",\"");
      sb.append(memoryMetrics.getActualFree()).append("\"]}");
      first = false;
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
    try {
      long rxBytes = 0;
      long txBytes = 0;
      List<NetworkMetrics> networkMetricsList = serverMetricsManager.getNetworkMetrics();
      for(NetworkMetrics eachMetrics: networkMetricsList) {
        rxBytes += eachMetrics.getRxBytes();
        txBytes += eachMetrics.getTxBytes();
      }
      if(!first) {
        sb.append(",");
      }
      sb.append("{\"network\":[\"").append(rxBytes);
      sb.append("\",\"").append(txBytes).append("\"]}");
      first = false;
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
    
    try {
      if(!first) {
        sb.append(",");
      }
      sb.append("{\"process\":[\"").append(serverMetricsManager.getProcessCount()).append("\"]}");
      first = false;
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
    sb.append("]");
    return sb.toString();
  }
  
  private String getMonitorItems(HttpServletRequest request) throws IOException {
    List<MonitorItem> items = Agent.getAgent().getItemManager().getMonitorItemsFromAdaptors();

    StringBuilder sb = new StringBuilder();
    for(MonitorItem eachItem: items) {
      sb.append(eachItem.toString()).append("<br/>");
    }
    
    return sb.toString();
  }

}