package org.cloumon.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.zookeeper.ZooKeeper;
import org.cloumon.manager.alarm.AgentFailMonitor;
import org.cloumon.manager.alarm.AlarmManager;
import org.cloumon.manager.servlet.MonitorControllerServlet;
import org.cloumon.manager.servlet.ZKControllerServlet;
import org.cloumon.thrift.MonitorItem;
import org.cloumon.thrift.MonitorService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gruter.common.conf.GruterConf;
import com.gruter.common.http.CommonHttpServer;
import com.gruter.common.server.ThriftApplicationServer;
import com.gruter.common.util.StringUtils;
import com.gruter.common.zk.DefaultWatcher;
import com.gruter.common.zk.ZKUtil;

public class MonitorManagerServer extends ThriftApplicationServer {
  public static final Log LOG = LogFactory.getLog(MonitorManagerServer.class);
  public static final String MONITOR_MANAGER_SERVICE_NAME = "monitor_manager";
  
  public static final int MIN_ITEM_PERIOD = 5; //60 sec
  public static final int DEFAULT_PORT = 9123;
  public static final int HTTP_PORT = 8123;
  
  protected MonitorServiceImpl monitorService;
  
  private CommonHttpServer webServer;

  private static ApplicationContext appCtx;

  private static MonitorManagerServer instance;
  
  protected HadoopMonitorItemLoader hadoopMonitorItemLoader;
  
  private AgentFailMonitor agentFailMonitor;
  
  public MonitorManagerServer(GruterConf conf, boolean clean) throws IOException {
    super(conf);
    MonitorManagerServer.instance = this;

    monitorService = getApplicationContext().getBean("monitorService", MonitorServiceImpl.class);
    hadoopMonitorItemLoader = new HadoopMonitorItemLoader(conf, this);
    hadoopMonitorItemLoader.loadHadoopItems();
    if(clean) {
      monitorService.clearAllDatas();
      loadInitialMonitorItems();
      hadoopMonitorItemLoader.saveMonitorItems();
      System.exit(0);
    }
    hadoopMonitorItemLoader.checkDataNode();
    monitorService.init(this);
    
    webServer = new CommonHttpServer("webapps", "0.0.0.0", HTTP_PORT, false);
    webServer.addServlet("monitor_json", "/monitor", MonitorControllerServlet.class);
    webServer.addServlet("zk_json", "/zkmanager", ZKControllerServlet.class);
    
    webServer.start();
    
    TableCreationThread tableCreationThread = new TableCreationThread();
    tableCreationThread.start();
   
    this.agentFailMonitor = new AgentFailMonitor(conf, zk, monitorService);
    try {
      this.agentFailMonitor.startMonitor();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
  
  public static ZooKeeper getZooKeeper() {
    return instance.zk;
  }
  
  public static MonitorService.Iface getMonitorService() {
    return instance.monitorService;
  }
  
  public static synchronized ApplicationContext getApplicationContext() {
    if(appCtx == null) {
      String[] configLocations = new String[] {"spring-conf.xml"};
      appCtx = new ClassPathXmlApplicationContext(configLocations);
    }
    return appCtx;
  }
  
  private void loadInitialMonitorItems() throws IOException {
    InputStream in = this.getClass().getClassLoader().getResourceAsStream("initial_monitor_items");
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String line;
    
    List<MonitorItem> monitorItems = new ArrayList<MonitorItem>();
    while ((line = reader.readLine()) != null) {
      try {
        if (line.trim().length() == 0 || line.trim().startsWith("#")) {
          LOG.info("skip comment line:" + line);
          continue;
        }
        MonitorItem item = parseMonitorItem(line);
        if (item.getPeriod() < MIN_ITEM_PERIOD) {
          LOG.warn("Monitor item's period must great than 60 sec.");
          item.setPeriod(60);
        }
        item.setDefaultItem(true);
        item.setDescription("inital monitor item");
        item.setAlarm(AlarmManager.getAlaramDBStr("$value < 0", "0", conf.get("alarm.mail.to", "test@test.com")));
        monitorItems.add(item);
      } catch (Exception e) {
        e.printStackTrace();
        throw new IOException(e.getMessage() + "[" + line + "]", e);
      }
    }
    try {
      monitorService.addMinitorItem(monitorItems);
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  private MonitorItem parseMonitorItem(String line) {
    MonitorItem monitorItem = new MonitorItem();
    String[] tokens = line.split(" ");
    
    monitorItem.setAdaptorClass(tokens[0]);
    if (tokens[1].indexOf(".") >= 0) {
      monitorItem.setGroupName(tokens[1].substring(0, tokens[1].indexOf(".")));
      monitorItem.setItemName(tokens[1].substring(tokens[1].indexOf(".") + 1));
    } else {
      monitorItem.setGroupName("nogroup");
      monitorItem.setItemName(tokens[1]);
    }
    
    monitorItem.setPeriod(Integer.parseInt(tokens[2]));
    
    String params = "";
    for (int i = 3; i < tokens.length; i++) {
      params += tokens[i] + " ";
    }
    monitorItem.setParams(params.trim());
    return monitorItem;
  }
  
  @Override
  protected TProcessor getProcessor() {
    return new MonitorService.Processor(monitorService);
  }

  @Override
  public int getServerPort() {
    return conf.getInt("cloumon.manager.port", DEFAULT_PORT);
  }

  @Override
  public String getServiceName() {
    return MONITOR_MANAGER_SERVICE_NAME;
  }

  class TableCreationThread extends Thread {
    @Override
    public void run() {
      try {
        Thread.sleep(60 * 1000);
      } catch (InterruptedException e) {
      }

      while(true) {
        try {
          monitorService.createTable();
        } catch (IOException e1) {
          LOG.error(e1.getMessage(), e1);
        }
        try {
          Thread.sleep(60 * 60 * 1000);     //1 hour
        } catch (InterruptedException e) {
          return;
        }     
      }
    }
  }
  
  public static void main(String[] args) {
//    args = new String[]{"-init"};
    try {
      GruterConf conf = new GruterConf();
      conf.addResource("cloumon-default.xml");
      conf.addResource("cloumon-site.xml");
      boolean clean = false;
      if (args.length > 0 && args[0].equals("-init")) {
        String zkPath = conf.get("zk.service.root", "/cloumon");
        System.out.print("Warn!!!!! All datas in ZooKeeper(" + zkPath + ") and DB will be removed. continue?(Y|N): ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String answer = reader.readLine();
        if("Y".equals(answer)) {
          clean = true;
          LOG.info("Started clear command.");
          ZooKeeper zk = new ZooKeeper(conf.get("zk.servers"), 10 * 1000, new DefaultWatcher());
          Thread.sleep(1 * 1000);
          ZKUtil.delete(zk, conf.get("zk.service.root", "/cloumon"), true);
          LOG.info("cleared all zookeeper node.");
          zk.close();        
        } else {
          LOG.info("Stoped clear command and starting server");
          System.exit(0);
        } 
      }

      StringUtils.startupShutdownMessage(MonitorManagerServer.class, args, LOG);
      MonitorManagerServer server = new MonitorManagerServer(conf, clean);
      server.startServer();
    } catch (Throwable e) {
      e.printStackTrace();
      LOG.error(StringUtils.stringifyException(e));
      System.exit(-1);
    }
  }

  @Override
  protected void zkDisconnected() {
    LOG.error("ZK DisConnected");
    super.zkDisconnected();
  }

  @Override
  protected void zkSessionExpired() {
    LOG.error("ZK Session Expired");
    super.zkSessionExpired();
  }
}
