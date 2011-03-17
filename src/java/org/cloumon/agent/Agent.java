package org.cloumon.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.cloumon.agent.item.ItemManager;
import org.cloumon.agent.item.server.ServerMetricsManager;
import org.cloumon.common.zk.ZKPath;
import org.cloumon.manager.MonitorManagerServer;
import org.cloumon.thrift.AgentService;
import org.cloumon.thrift.CollectorService;
import org.cloumon.thrift.HostInfo;
import org.cloumon.thrift.MetricRecord;
import org.cloumon.thrift.MonitorItem;
import org.cloumon.thrift.MonitorService;

import com.gruter.common.conf.GruterConf;
import com.gruter.common.http.CommonHttpServer;
import com.gruter.common.server.ThriftApplicationServer;
import com.gruter.common.server.service.ServiceLocator;
import com.gruter.common.server.thrift.NoServerException;
import com.gruter.common.server.thrift.ThriftConnection;
import com.gruter.common.server.thrift.ThriftConnectionPool;
import com.gruter.common.server.thrift.ThriftConnectionPoolFactory;
import com.gruter.common.util.StringUtils;

public class Agent extends ThriftApplicationServer implements AgentService.Iface, Runnable  {
  public static Log LOG = LogFactory.getLog(Agent.class);
  public static final int DEFAULT_PORT = 9125;

//  static final String HOST_NAME_FILE = "cloumon_hostname";
  
  private ItemManager itemManager;
  private Thread agentThread;
  private AtomicBoolean stop;
  private ThriftConnectionPool collectorPool;
  private ThriftConnectionPool managerPool;
  private String nodeType;
  
  private long startTime;
  
  private static Agent instance;
  
  public Agent(GruterConf conf, boolean init) throws IOException {
    super(conf);
    
//    if(init) {
//      initAgent();
//      return;
//    }
    instance = this;
    
    CommonHttpServer webServer = new CommonHttpServer("webapps", "0.0.0.0", conf.getInt("cloumon.agent.httpPort", 8124), false);
    webServer.addServlet("agent_status", "/agent", AgentStatusServlet.class);
    webServer.start();
    
    this.startTime = System.currentTimeMillis();
  }

  public static Agent getAgent() {
    return instance;
  }

  public long getStartTime() {
    return startTime;
  }
  
  public ThriftConnectionPool getManagerPool() {
    return managerPool;
  }
  
  public ItemManager getItemManager() {
    return itemManager;
  }

  @Override
  public void run() {
    stop = new AtomicBoolean(false);
    while (!stop.get()) {
      try {
        synchronized(stop) {
          stop.wait();
        }
      } catch (InterruptedException e) {
      }
    }
    LOG.warn("Agent stopped called by stop()");
  }

//  private String getHostNameFilePath() {
//    String path = conf.get("cloumon.agent.hostNamePath", "/tmp/cloumon");
//    if(path.length() > 0 && !path.endsWith("/")) {
//      path += "/";
//    }
//    
//    return path;
//  }
//  
//  private boolean isRegisteredHost() throws IOException {
//    File file = new File(getHostNameFilePath(), HOST_NAME_FILE);
//    if (file.exists()) {
//      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
//      String hostName = reader.readLine();
//      if (hostName == null) {
//        return false;
//      } else {
//        return true;
//      }
//    } else {
//      return false;
//    }
//  }
//  
//  private void saveHostNameToFile() throws IOException {
//    File parentFile = new File(getHostNameFilePath());
//    File file = new File(parentFile, HOST_NAME_FILE);
//    if(!parentFile.exists()) {
//      try {
//        parentFile.mkdirs();
//      } catch (Exception e) {
//        LOG.fatal("Can't make agent id path:" + file + ", check cloumon.agent.hostNamePath property in conf");
//        System.exit(0);
//      }
//    }
//    OutputStream out = new FileOutputStream(file);
//    out.write((hostName + "\n").getBytes());
//    out.close();
//  }

  private void registerHost() throws IOException {
    ServerMetricsManager serverMetricsManager = null;
    HostInfo hostInfo = null;
    try {
      serverMetricsManager = new ServerMetricsManager();
      hostInfo = serverMetricsManager.getHostInfo();
      hostInfo.setHostName(hostName);
      hostInfo.setLiveStatus(true);
      hostInfo.setAlarmOn(true);
      hostInfo.setHostAlarm(conf.get("alarm.mail.to", "test@test.com"));
    } finally {
      if (serverMetricsManager != null) {
        serverMetricsManager.close();
      }
    }

    while (true) {
      ThriftConnection managerConnection = null;
      try {
        managerConnection = managerPool.getConnection();
        MonitorService.Client manager = (MonitorService.Client)managerConnection.getThriftServiceClient();
        hostInfo.setHostType(nodeType);
        manager.registerAgent(hostIp, hostInfo);
        //saveHostNameToFile();
        return;
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
        try {
          Thread.sleep(10 * 1000);
        } catch (InterruptedException e1) {
        }
      } finally {
        if (managerConnection != null) {
          managerPool.releaseConnection(managerConnection);
        }
      }
    }
  }
  
//  private void updateLiveStatus() throws IOException {
//    while (true) {
//      ThriftConnection managerConnection = null;
//      try {
//        managerConnection = managerPool.getConnection();
//        MonitorService.Client manager = (MonitorService.Client)managerConnection.getThriftServiceClient();
//        LOG.info("updateAgentLiveStatus:" + hostName);
//        manager.updateAgentLiveStatus(hostName, true);
//        return;
//      } catch (Exception e) {
//        if (managerConnection != null) {
//          try {
//            managerPool.closeConnection(managerConnection);
//          } catch (Exception e1) {
//          }
//        }
//        managerConnection = null;
//        LOG.error(e.getMessage(), e);
//        try {
//          Thread.sleep(10 * 1000);
//        } catch (InterruptedException e1) {
//        }
//      } finally {
//        if (managerConnection != null) {
//          managerPool.releaseConnection(managerConnection);
//        }
//      }
//    }
//  }
  
  public void sendMonitorRecords(List<MetricRecord> metricsRecords) throws IOException {
    long startTime = System.currentTimeMillis();
    Exception exception = null;
    while (true) {
      ThriftConnection connection = null;
      try {
        connection = collectorPool.getConnection();
        CollectorService.Client client = (CollectorService.Client)connection.getThriftServiceClient();
        client.addMetricRecord(metricsRecords);
        
        return;
      } catch (NoServerException e) {
        if (connection != null) {
          try {
            collectorPool.closeConnection(connection);
          } catch (Exception e1) {
          }
          connection = null;
        }
        return;
      } catch (Exception e) {
        if (connection != null) {
          try {
            collectorPool.closeConnection(connection);
          } catch (Exception e1) {
          }
          connection = null;
        }
        exception = e;
      } finally {
        if (connection != null) {
          collectorPool.releaseConnection(connection);
        }
      }
      long time = System.currentTimeMillis();
      if (time - startTime > 30 * 1000) {
        LOG.error("Timeout while sendMonitorRecord, last error is: " + exception.getMessage(), exception);
        throw new IOException("Timeout while sendMonitorRecord, last error is: " + exception.getMessage(), exception);
      }
    }
  }
  
//  private void initAgent() {
//    File parentFile = new File(getHostNameFilePath());
//    File file = new File(parentFile, HOST_NAME_FILE);
//    file.delete();
//  }
  
  public static void main(String[] args) throws Exception {
    GruterConf conf = new GruterConf();
    conf.addResource("cloumon-default.xml");
    conf.addResource("cloumon-site.xml");

    try {
//      if (args.length > 0 && args[0].equals("-init")) {
//        System.out.print("Warn!!!!! Agent init. continue?(Y|N): ");
//        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//        String answer = reader.readLine();
//        if("Y".equals(answer)) {
//          Agent agent = new Agent(conf, true);
//        }
//         System.exit(0);
//      }
      StringUtils.startupShutdownMessage(Agent.class, args, LOG);
      Agent agent = new Agent(conf, false);
      agent.startServer();
      //agent.join();
    } catch (Throwable e) {
      LOG.error(StringUtils.stringifyException(e));
      System.exit(-1);
    }
  }

  @Override
  public String getServiceName() {
    return ZKPath.ZK_AGENT_SERVICE_NAME;
  }

  @Override
  protected TProcessor getProcessor() {
    return new AgentService.Processor(this);
  }

  @Override
  public int getServerPort() {
    return conf.getInt("agent.port", DEFAULT_PORT);
  }

  @Override
  protected void zkDisconnected() {
  }

  @Override
  protected void zkSessionExpired() {
  }

  @Override
  protected void zkConnected() throws IOException {
  }

  @Override
  public void addMonitorItems(List<MonitorItem> monitorItems) throws TException {
  }

  @Override
  public void startServer() throws IOException {
    nodeType = conf.get("cloumon.agent.nodeType", "host");
    collectorPool = ThriftConnectionPoolFactory.getInstance(conf).getPool(
        new ServiceLocator(conf, ZKPath.ZK_COLLECTOR_SERVICE_NAME, 10 * 1000), CollectorService.Client.class);
    managerPool = ThriftConnectionPoolFactory.getInstance(conf).getPool(
        new ServiceLocator(conf, MonitorManagerServer.MONITOR_MANAGER_SERVICE_NAME, 10 * 1000), MonitorService.Client.class);

//    if (isRegisteredHost()) {
//      updateLiveStatus();
//    } else {
//      registerHost();
//    }
    registerHost();
    
    agentThread = new Thread(this, "Agent");
    agentThread.setDaemon(false);
    agentThread.start();
    
    itemManager = new ItemManager(this, zk);
    super.startServer();
  }
}