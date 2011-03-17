package org.cloumon.manager.alarm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.cloumon.common.zk.ZKPath;
import org.cloumon.manager.MonitorServiceImpl;
import org.cloumon.thrift.HostInfo;

import com.gruter.common.conf.GruterConf;
import com.gruter.common.util.StringUtils;
import com.gruter.common.zk.ZKUtil;

//TODO 주기적으로 장애가 발생한 서버에 대해 알림을 전송하는 기능 추가
public class AgentFailMonitor {
  private static final Log LOG = LogFactory.getLog(AgentFailMonitor.class);
  
  private GruterConf conf;
  private ZooKeeper zk;
  private String servicePath;
  
  private List<String> previousAgents;
  
  private AgentMonitorWatcher agentMonitorWatcher;
  
  private List<AlarmSender> alarmSenders;
  
  private MonitorServiceImpl  monitorService;
  
  private Object lock = new Object();
  
  public AgentFailMonitor(GruterConf conf, ZooKeeper zk, MonitorServiceImpl monitorService) {
    this.conf = conf;
    this.zk = zk;
    this.monitorService = monitorService;
    this.servicePath = ZKUtil.getServicePath(conf, ZKPath.ZK_AGENT_SERVICE_NAME);
    this.agentMonitorWatcher = new AgentMonitorWatcher();
    this.alarmSenders = AlarmManager.getSenders(conf);
  }
  
  public void startMonitor() throws Exception {
    synchronized(lock) {
      try {
        previousAgents = zk.getChildren(servicePath, agentMonitorWatcher);
      } catch (KeeperException.NoNodeException e) {
        ZKUtil.createNode(zk, servicePath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, true);
      }
      if(previousAgents == null) {
        previousAgents = new ArrayList<String>();
      }
      List<HostInfo> hostInfos = monitorService.findAllHosts();
      Set<String> zkHosts = new HashSet<String>();
      for(String eachZkHost: previousAgents) {
        zkHosts.add(StringUtils.getHostName(eachZkHost));
      }
      
      for(HostInfo eachHostInfo: hostInfos) {
        if(!zkHosts.contains(eachHostInfo.getHostName())) {
          monitorService.updateAgentLiveStatus(eachHostInfo.getHostName(), false);
          for(AlarmSender eachSender: alarmSenders) {
            eachSender.sendAgentFailAlarm(conf, eachHostInfo);
          }
        }
      }
    }
  }
  
  private void sendAlarm(List<String> removedAgents) {
    synchronized(lock) {
      for(String eachAgent: removedAgents) {
        byte[] hostNamePortBytes = null;
        try {
          hostNamePortBytes = zk.getData(servicePath + "/" + eachAgent, false, new Stat());
        } catch (Exception e) {
          LOG.error("error get host data in zk node:" + servicePath + "/" + eachAgent + ":" + e.getMessage(), e);
          continue;
        }
        if(hostNamePortBytes == null) {
          LOG.error("No host data in zk node:" + servicePath + "/" + eachAgent);
          continue;
        }
        String hostName = StringUtils.getHostName(new String(hostNamePortBytes));
        HostInfo hostInfo = null;
        try {
          hostInfo = monitorService.getHostInfo(hostName);
        } catch (TException e) {
          LOG.error("Error get HostInfo:" + hostInfo);
          continue;
        }
        if(hostInfo == null) {
          LOG.warn("No hostInfo:" + hostName + " while sending failed agent notification");
          continue;
        }
        for(AlarmSender eachSender: alarmSenders) {
          eachSender.sendAgentFailAlarm(conf, hostInfo);
        }
      }
    }
  }
  
  class AgentMonitorWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
      if(event.getType() == Event.EventType.NodeChildrenChanged) {
        try {
          List<String> currentAgents = zk.getChildren(servicePath, agentMonitorWatcher);
          List<String> added = new ArrayList<String>();
          List<String> removed = new ArrayList<String>();
          ZKUtil.compareCollection(previousAgents, currentAgents, added, removed);
          previousAgents = currentAgents;
          sendAlarm(removed);
        } catch (Exception e) {
          LOG.error(e.getMessage(), e);
        }
      }
    }
  }
}
