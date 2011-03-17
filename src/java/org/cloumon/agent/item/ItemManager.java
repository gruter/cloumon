package org.cloumon.agent.item;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.cloumon.agent.Agent;
import org.cloumon.common.util.CollectionUtils;
import org.cloumon.common.zk.ZKPath;
import org.cloumon.thrift.MonitorItem;
import org.cloumon.thrift.MonitorService;

import com.gruter.common.conf.GruterConf;
import com.gruter.common.server.thrift.ThriftConnection;
import com.gruter.common.zk.ZKUtil;

public class ItemManager implements Watcher {
  static final Log LOG = LogFactory.getLog(ItemManager.class);
  public static String DEFAULT_ITEM_PATH = "/default_items";
  
  public static Map<String, String> adaptorClasses = new HashMap<String, String>();
  
  static GruterConf conf;
  static {
    conf = new GruterConf();
    conf.addResource("cloumon-default.xml");
    conf.addResource("cloumon-site.xml");
  }
  
  static {
    String adaptorConfDefault = SystemMonitorAdaptor.class.getSimpleName() + "=" + SystemMonitorAdaptor.class.getCanonicalName() + "," +
                          ExecAdaptor.class.getSimpleName() + "=" + ExecAdaptor.class.getCanonicalName();
    
    String adaptorConf = conf.get("cloumon.item.adaptor", adaptorConfDefault);
    
    String[] adaptorTokens = adaptorConf.split(",");
    
    for(String eachAdaptorToken: adaptorTokens) {
      String[] adaptorKeyValueToken = eachAdaptorToken.split("=");
      adaptorClasses.put(adaptorKeyValueToken[0], adaptorKeyValueToken[1]);
    }
  }
  
  private List<MonitorItemAdaptor> adaptors = new ArrayList<MonitorItemAdaptor>();

  private ZooKeeper zk;
  private Agent agent;
//  private String defaultItemPath;
  private String hostItemPath; 
  
  public ItemManager(Agent agent, ZooKeeper zk) throws IOException {
    this.zk = zk;
    this.agent = agent;

    createHostMonitorItemRootPath();
    addAdaptors();
    //addAdaptors(true);
  }

  private void createHostMonitorItemRootPath() throws IOException {
    try {
      hostItemPath = getHostMonitorItemChangedDir(conf, agent.getHostName());
      if (zk.exists(hostItemPath, false) == null) {
        ZKUtil.createNode(zk, hostItemPath, null, ZKUtil.ZK_ACL, CreateMode.PERSISTENT, true);
      }
      
      zk.exists(hostItemPath, this);
    } catch (Exception e) {
      throw new IOException(e.getMessage(), e);
    }
  }
  
  private List<MonitorItem> getAgentMonitorItems() throws IOException {
    ThriftConnection conn = null;
    try {
      conn = agent.getManagerPool().getConnection();
      MonitorService.Client monitorService = (MonitorService.Client)conn.getThriftServiceClient();
      List<MonitorItem> monitorItems = monitorService.findHostMonitorItems(agent.getHostName());
      return monitorItems;
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new IOException(e.getMessage(), e);
    } finally {
      if(conn != null) {
        agent.getManagerPool().releaseConnection(conn);
      }
    }
  }
  
  private void addAdaptors() throws IOException {
    List<MonitorItem> items = getAgentMonitorItems();
    if(items != null) {
      for (MonitorItem eachItem: getAgentMonitorItems()) {
        addAdaptor(eachItem);
      }
    }
  }

  private void removeAdaptor(MonitorItem monitorItem) throws IOException {
    synchronized(adaptors) {
      int removeTarget = -1;
      int index = 0;
      for(MonitorItemAdaptor eachAdaptor: adaptors) {
        int matchedIndex = eachAdaptor.searchMonitorItem(monitorItem.getItemId());
        if(matchedIndex >= 0) {
          LOG.info("MonitorItem removed: " + monitorItem.getItemId() + ", " + eachAdaptor.getItemNames());
          eachAdaptor.removeMonitorItem(matchedIndex);
          if(eachAdaptor.getMonitorItems().size() == 0) {
            LOG.info("Item Group stoped");
            removeTarget = index;
            eachAdaptor.stop();
          }
          break;
        }
        index++;
      }
      if(removeTarget >= 0) {
        adaptors.remove(removeTarget);
      }
    }
  }
  
  private void addAdaptor(MonitorItem monitorItem) throws IOException {
    try {
      MonitorItemAdaptor adaptor = createAdaptor(monitorItem);
      MonitorItemAdaptor groupAdaptor = findMonitorItemAdaptor(adaptor);
      
      if (groupAdaptor != null) {
        LOG.info("Monitor Item added in existed GroupItem: " + monitorItem.getGroupName() + "." + monitorItem.getItemName());
        groupAdaptor.addMonitorItem(monitorItem);
        return;
      } else {
        LOG.info("Monitor Item added: " + monitorItem.getGroupName() + "." + monitorItem.getItemName());
        adaptor.start();
        synchronized(adaptors) {
          adaptors.add(adaptor);
        }
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      //throw new IOException(e.getMessage(), e);
    }
  }
  
  private MonitorItemAdaptor findMonitorItemAdaptor(MonitorItemAdaptor monitorItemAdaptor) {
    synchronized(adaptors) {
      for (MonitorItemAdaptor eachAdaptor: adaptors) {
        if (eachAdaptor.equals(monitorItemAdaptor)) {
          return eachAdaptor;
        }
      }
    }
    
    return null;
  }
  
  @SuppressWarnings("unchecked")
  private MonitorItemAdaptor createAdaptor(MonitorItem monitorItem) throws IOException {
    try {
      String adaptorClassName = adaptorClasses.get(monitorItem.getAdaptorClass());
      if(adaptorClassName == null) {
        adaptorClassName = monitorItem.getAdaptorClass();
      }
      Class<? extends MonitorItemAdaptor> monitorAdaptorClass = 
          (Class<? extends MonitorItemAdaptor>)Class.forName(adaptorClassName);
      
      Constructor<? extends MonitorItemAdaptor> constrictor = 
        monitorAdaptorClass.getConstructor(Agent.class, MonitorItem.class);  
      MonitorItemAdaptor adaptor = constrictor.newInstance(agent, monitorItem);
      return adaptor;
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new IOException(e.getMessage(), e);
    }
  }

  public List<MonitorItem> getMonitorItemsFromAdaptors() {
    List<MonitorItem> result = new ArrayList<MonitorItem>();
    synchronized(adaptors) {
      for (MonitorItemAdaptor eachAdaptor: adaptors) {
        result.addAll(eachAdaptor.getMonitorItems());
      }
    }
    
    return result;
  }
  
  private void itemChanged() {
    try {
      List<Object> addedItems = new ArrayList<Object>();
      List<Object> removedItems = new ArrayList<Object>();
      
      List<MonitorItem> originItems = getMonitorItemsFromAdaptors();
      List<MonitorItem> currentItems = getAgentMonitorItems();
      CollectionUtils.compareList(originItems, currentItems, addedItems, removedItems);
      
      for (Object removedItem: removedItems) {
        removeAdaptor((MonitorItem)removedItem);
      }
      
      for (Object addedItem: addedItems) {
        addAdaptor((MonitorItem)addedItem);
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
  
//  public static String getDefaultMonitorItemChangedDir(GruConfiguration conf) {
//    return ZKUtil.getServicePath(conf, ZKUtil.ZK_MONITOR_SERVICE_NAME) + 
//      ZKUtil.ZK_MINITOR_EVENT_PATH + "/default";
//  }

  public static String getHostMonitorItemChangedDir(GruterConf conf, String hostName) {
    return ZKUtil.getServicePath(conf, ZKPath.ZK_MONITOR_SERVICE_NAME) + 
      ZKPath.ZK_MINITOR_EVENT_PATH + "/" + hostName;
  }
  
  @Override
  public void process(WatchedEvent event) {
    String eventPath = event.getPath();
    if (eventPath == null) {
      return;
    }
    
//    if (eventPath.equals(defaultItemPath) ||
    if(eventPath.equals(hostItemPath)) {
      if(event.getType() == Event.EventType.NodeDataChanged) {
        try {
          zk.exists(eventPath, this);
        } catch (KeeperException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        itemChanged();
      }
      return;
    }
  }
}
