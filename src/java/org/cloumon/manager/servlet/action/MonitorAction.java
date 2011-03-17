package org.cloumon.manager.servlet.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public abstract class MonitorAction {
  protected abstract String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException;
  
  public String runAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    return doAction(request, response);
//    ThriftConnection conn = null;
//    try {
//      return doAction(request, response);
//    } catch (Exception e) {
//      //TODO Exception을 구분하여 Pool remove 처리여부
//      //TODO retry 기능 추가
//      MonitorControllerServlet.pool.removeFromPool(conn);
//      throw new IOException(e.getMessage(), e);
//    } finally {
//      if(conn != null) {
//        MonitorControllerServlet.pool.releaseConnection(conn);
//      }
//    }
  }
}
