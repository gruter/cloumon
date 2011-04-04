package org.cloumon.manager.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.cloumon.manager.MonitorManagerServer;
import org.cloumon.manager.servlet.action.ActionFactory;
import org.cloumon.manager.servlet.action.MonitorAction;
import org.cloumon.thrift.MonitorService;

import com.gruter.common.conf.GruterConf;
import com.gruter.common.server.service.ServiceLocator;
import com.gruter.common.server.thrift.ThriftConnectionPool;
import com.gruter.common.server.thrift.ThriftConnectionPoolFactory;
import com.gruter.common.zk.ZKUtil;

/**
 * 상세조회 화면에 필요한 데이터 모델
 * Map<String(HostName), TreeMap<Date, HashMap<String(group), Map<String(item), Object(data)>>>>
 *  -> thrift를 위한 모델로 변환(Set<ResultRecord>는 정렬되어 있음))
 * Map<HostName, Set<ResultRecord>>
 *   ResultRecord =  Date, HashMap<String(group), Map<String(item), Object(data)>>>>
 * @author babokim
 *
 */
@SuppressWarnings("serial")
//http://techbug.tistory.com/13
public class MonitorControllerServlet extends HttpServlet {
  private static final Log LOG = LogFactory.getLog(MonitorControllerServlet.class);
  
  public static ThriftConnectionPool pool;
  public static GruterConf conf;
  public static ZooKeeper zk;
  
  Object initMonitor = new Object();
  
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    response.setContentType("text/html;charset=UTF-8");
    
    PrintWriter out = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"), true);//response.getWriter();

    MonitorAction action = ActionFactory.getAction(request);
    LOG.info("action:" + action);

    try {
      if(action != null) {
        String actionResult = action.runAction(request, response);
        LOG.debug("actionResult: " + actionResult);
        out.write(actionResult);
      } else {
        out.write("ERROR:No avaliable action!!! Check action parameter");
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      out.write(e.getMessage());
      //throw new ServletException(e.getMessage(), e);
    } finally {
      out.close();
    }
  }
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    synchronized(initMonitor) {
      if(conf == null) {
        conf = new GruterConf();
        conf.addResource("cloumon-default.xml");
        conf.addResource("cloumon-site.xml");

        try {
          pool = ThriftConnectionPoolFactory.getInstance(conf).getPool(
              new ServiceLocator(conf, MonitorManagerServer.MONITOR_MANAGER_SERVICE_NAME, 10 * 1000), MonitorService.Client.class);
          final CountDownLatch latch = new CountDownLatch(1);
          zk = ZKUtil.connectZK(conf, MonitorManagerServer.MONITOR_MANAGER_SERVICE_NAME, new Watcher() {
  
            @Override
            public void process(WatchedEvent event) {
              if(event.getType() == Event.EventType.None) {
                switch(event.getState()) {
                case SyncConnected:
                  LOG.info("ZK Connected");
                  latch.countDown();
                  break;
                case Disconnected: 
                  LOG.info("ZK Disconnected");
                  break;
                case Expired:
                  LOG.info("ZK Expired");
                  break;
                }
              }
            }
          });
          
          try {
            latch.await();
          } catch (InterruptedException e) {
          }
        } catch (IOException e) {
          throw new ServletException(e.getMessage(), e);
        }
      }
    }
  }
}  
  
