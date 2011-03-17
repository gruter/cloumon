package org.cloumon.manager.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

@SuppressWarnings("serial")
public class ZKControllerServlet extends HttpServlet {
  private static final Log LOG = LogFactory.getLog(ZKControllerServlet.class);
  private static final int MAX_CHILD_COUNT = 500;
  
  private Map<String, ZooKeeper> zookeepers = new HashMap<String, ZooKeeper>();
  Object zkConnMonitor = new Object();

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String zkservers = request.getParameter("zkservers");
    if (zkservers == null) {
      throw new IOException("No zkservers paramter");
    }
    String path = request.getParameter("dir");
    if (path == null) {
      throw new IOException("No dir parameter");
    }
    
    LOG.info("zkservers:" + zkservers + ", dir:" + path);
    
    PrintWriter out = null;
    try {
      out = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"), true);

      String action = request.getParameter("action");
      String result = "";
      if(action != null && action.equals("GetZKNodeDetail")) {
        result = getNodeDetail(zkservers, path);
      } else {
        result = getTreeNodes(zkservers, path);
      }
      
      LOG.info("zk:" + result);
      response.setContentType("text/html;charset=UTF-8");

      out.write(result);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      out.write(e.getMessage());
      // throw new ServletException(e.getMessage(), e);
    } finally {
      out.close();
    }
  }

  private String getNodeDetail(String zkservers, String path) throws Exception {
    if ("/".equals(path)) {
    } else if (path.endsWith("/")) {
      path = path.substring(0, path.getBytes().length- 1);
    }
    
    StringBuilder sb = new StringBuilder();
    ZooKeeper zk = getZooKeeper(zkservers);
    
    Stat stat = zk.exists(path, false);
    if(stat == null) {
      return "No Node";
    }
    
    byte[] data = zk.getData(path, false, new Stat());
    
    sb.append("[");
    String dataValue = data == null ? "null" : new String(data);
    sb.append("{name: \"data\", value:\"").append(dataValue).append("\"},");    
    sb.append("{name: \"numChildren\", value:\"").append(stat.getNumChildren()).append("\"},");
    sb.append("{name: \"ephemeralOwner\", value:\"").append(stat.getEphemeralOwner()).append("\"},");
    sb.append("{name: \"version\", value:\"").append(stat.getVersion()).append("\"},");
    sb.append("{name: \"cversion\", value:\"").append(stat.getCversion()).append("\"},");
    sb.append("{name: \"aversion\", value:\"").append(stat.getAversion()).append("\"},");
    sb.append("{name: \"czxid\", value:\"").append(stat.getCzxid()).append("\"},");
    sb.append("{name: \"mzxid\", value:\"").append(stat.getMzxid()).append("\"},");
    sb.append("{name: \"pzxid\", value:\"").append(stat.getPzxid()).append("\"},");
    sb.append("{name: \"ctime\", value:\"").append(stat.getCtime()).append("\"},");
    sb.append("{name: \"mtime\", value:\"").append(stat.getMtime()).append("\"}");
    sb.append("]");
    
    return sb.toString();
  }
  
  private String getTreeNodes(String zkservers, String dir) throws Exception, KeeperException, InterruptedException {
    StringBuilder sb = new StringBuilder();
    if ("_root_/".equals(dir)) {
      sb.append("<ul class=\"jqueryFileTree\" style=\"display: none;\">");
      sb.append("<li class=\"directory collapsed\"><a href=\"#\" rel=\"/\">/</a></li>");
      sb.append("</ul>");
      return sb.toString();
    }

    if ("/".equals(dir)) {
    } else if (dir.charAt(dir.length() - 1) == '/') {
      dir = dir.substring(0, dir.length() - 1);
    }

    ZooKeeper zk = getZooKeeper(zkservers);
    
    Stat stat = zk.exists(dir, false);
    if (stat != null) {
      List<String> paths = zk.getChildren(dir, false);
      Collections.sort(paths);
      if(dir.equals("/")) {
        dir = "";
      }
      if (paths != null) {
        sb.append("<ul class=\"jqueryFileTree\" style=\"display: none;\">");
          
        int numChild = stat.getNumChildren();
        int count = 1;
        for (String path : paths) {
          String fullPath = dir + "/" + path;
          if(count < numChild && numChild >= MAX_CHILD_COUNT) {
            path += "(" + (numChild - count) + " remain)";
          }
          sb.append("<li class=\"directory collapsed\"><a href=\"#\" rel=\"" + fullPath + "/\">" + path + "</a></li>");
          count++;
          if(count >= MAX_CHILD_COUNT) {
            break;
          }
        }
      }
    }
    sb.append("</ul>");
    return sb.toString();
  }

  private String getTreeNodes2(String zkservers, String path) throws Exception, KeeperException, InterruptedException {
    StringBuffer sb = new StringBuffer();
    ZooKeeper zk = getZooKeeper(zkservers);
    List<String> paths = zk.getChildren(path, false);

    sb.append("[");
    if (path.equals("/")) {
      path = "";
    }

    int size = paths.size();
    for (int i = 0; i < size; i++) {
      String eachPath = paths.get(i);
      List<String> childPath = zk.getChildren(path + "/" + eachPath, false);
      sb.append("{ \"data\":{\"title\":\"").append(eachPath).append("\",");
      sb.append("\"attr\":{\"href\":\"#\", \"id\":\"").append(path + "/" + eachPath).append("\"}}}");
      if (i < size - 1) {
        sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }

  private String getTreeNodes1(String zkservers, String path) throws Exception, KeeperException, InterruptedException {
    StringBuffer sb = new StringBuffer();
    ZooKeeper zk = getZooKeeper(zkservers);
    List<String> paths = zk.getChildren(path, false);

    sb.append("[");
    if (path.equals("/")) {
      path = "";
    }

    int size = paths.size();
    for (int i = 0; i < size; i++) {
      String eachPath = paths.get(i);
      List<String> childPath = zk.getChildren(path + "/" + eachPath, false);
      sb.append("{data:\"").append(eachPath).append("\",");
      sb.append("value:\"").append(path + "/" + eachPath).append("\",");
      sb.append("children:").append(listToJson(path + "/" + eachPath, childPath)).append("}");
      if (i < size - 1) {
        sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }

  private String listToJson(String parentPath, List<String> children) {
    try {
      StringBuilder sb = new StringBuilder();

      sb.append("[");
      int size = children.size();
      for (int i = 0; i < size; i++) {
        sb.append("{data:\"").append(children.get(i)).append("\",");
        sb.append("value:\"").append(parentPath + "/" + children.get(i)).append("\"}");

        if (i < size - 1) {
          sb.append(",");
        }
      }
      sb.append("]");
      return sb.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  private ZooKeeper getZooKeeper(final String zkservers) throws Exception {
    synchronized (zookeepers) {
      if (zookeepers.containsKey(zkservers)) {
        LOG.info("Get ZK from pool");
        return zookeepers.get(zkservers);
      }
      Watcher watcher = new ZKMonitorWatcher(zkservers);

      final ZooKeeper zk = new ZooKeeper(zkservers, 30 * 1000, watcher);

      synchronized (zkConnMonitor) {
        zkConnMonitor.wait();
      }
      zookeepers.put(zkservers, zk);
      LOG.info("Create new ZK conneciton");
      return zk;
    }
  }

  class ZKMonitorWatcher implements Watcher {
    private String zkservers;

    public ZKMonitorWatcher(String zkservers) {
      this.zkservers = zkservers;
    }

    @Override
    public void process(WatchedEvent event) {
      if (event.getType() == Event.EventType.None) {
        switch (event.getState()) {
        case SyncConnected:
          LOG.info("ZK Connected:" + zkservers);
          synchronized (zkConnMonitor) {
            zkConnMonitor.notifyAll();
          }
          break;
        case Disconnected:
          LOG.info("ZK Disconnected:" + zkservers);
          break;
        case Expired:
          LOG.info("ZK Expired:" + zkservers);
          synchronized (zookeepers) {
            zookeepers.remove(zkservers);
          }
          break;
        }
      }
    }
  }
}
