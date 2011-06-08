package org.cloumon.manager.alarm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  
  private Map<String, String> ipToHostNames = new HashMap<String, String>();
  
  private List<String> previousAgents;
  
  private List<String> previousAgentIps;
  
  private AgentMonitorWatcher agentMonitorWatcher;
  
  private List<AlarmSender> alarmSenders;
  
  private MonitorServiceImpl  monitorService;
  
  private Object lock = new Object();
  
  public AgentFailMonitor(GruterConf conf, ZooKeeper zk, MonitorServiceImpl monitorService) {
    this.conf = conf;
    this.zk = zk;
    this.monitorService = monitorService;
    this.servicePath = ZKUtil.getServiceLiveServerPath(conf, ZKPath.ZK_AGENT_SERVICE_NAME);
    this.agentMonitorWatcher = new AgentMonitorWatcher();
    this.alarmSenders = AlarmManager.getSenders(conf);
  }
  
  public void startMonitor() throws Exception {
    LOG.info("Start Agent Fail Manager");
    synchronized(lock) {
      //List<String> agentHostIps = null;
      try {
        previousAgentIps = zk.getChildren(servicePath, agentMonitorWatcher);
      } catch (KeeperException.NoNodeException e) {
        ZKUtil.createNode(zk, servicePath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, true);
      }
      if(previousAgents == null) {
        previousAgents = new ArrayList<String>();
      }
      
      if(previousAgentIps != null) {
        for(String hostIp: previousAgentIps) {
          byte[] data = zk.getData(servicePath + "/" + hostIp, false, new Stat());
          if(data != null) {
            String hostName = new String(data);
            ipToHostNames.put(hostIp, hostName);
            previousAgents.add(hostName);
          }
        }
      }
      List<HostInfo> hostInfos = monitorService.findAllHosts();
      Set<String> zkHosts = new HashSet<String>();
      for(String eachZkHost: previousAgents) {
//        LOG.info("zkHost:" + StringUtils.getHostName(eachZkHost) + ">" + eachZkHost);
        zkHosts.add(StringUtils.getHostName(eachZkHost));
      }
      
      for(HostInfo eachHostInfo: hostInfos) {
//        LOG.info("DB:" + eachHostInfo.getHostName());
        if(!zkHosts.contains(eachHostInfo.getHostName())) {
          monitorService.updateAgentLiveStatus(eachHostInfo.getHostName(), false);
          for(AlarmSender eachSender: alarmSenders) {
            eachSender.sendAgentFailAlarm(conf, eachHostInfo);
          }
        } else {
          monitorService.updateAgentLiveStatus(eachHostInfo.getHostName(), true);
        }
      }
    }
  }
  
  private void sendAlarm(List<String> removedAgents) {
    synchronized(lock) {
      for(String eachAgent: removedAgents) {
//        byte[] hostNamePortBytes = null;
//        try {
//          hostNamePortBytes = zk.getData(servicePath + "/" + eachAgent, false, new Stat());
//        } catch (Exception e) {
//          LOG.error("error get host data in zk node:" + servicePath + "/" + eachAgent + ":" + e.getMessage(), e);
//          continue;
//        }
//        if(hostNamePortBytes == null) {
//          LOG.error("No host data in zk node:" + servicePath + "/" + eachAgent);
//          continue;
//        }
//        String hostName = StringUtils.getHostName(new String(hostNamePortBytes));
        String hostName = ipToHostNames.get(eachAgent);
        if(hostName == null) {
          LOG.warn("No hostname info for agent:" + eachAgent);
          continue;
        }
        if(hostName.indexOf(":") > 0) {
          hostName = StringUtils.getHostName(new String(hostName));
        }
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
        try {
          monitorService.updateAgentLiveStatus(hostInfo.getHostName(), false);
        } catch (TException e) {
          LOG.error(hostInfo.getHostName() + ":" + e.getMessage(), e);
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
          List<String> currentAgentIps = zk.getChildren(servicePath, agentMonitorWatcher);
          List<String> added = new ArrayList<String>();
          List<String> removed = new ArrayList<String>();
          
          LOG.info("Previous Hosts:" + previousAgentIps.size() + ", Current Hosts:" + currentAgentIps.size());
          ZKUtil.compareCollection(previousAgentIps, currentAgentIps, added, removed);
          previousAgentIps = currentAgentIps;
          if(previousAgentIps != null) {
            for(String hostIp: previousAgentIps) {
              byte[] data = zk.getData(servicePath + "/" + hostIp, false, new Stat());
              if(data != null) {
                String hostName = new String(data);
                ipToHostNames.put(hostIp, hostName);
                previousAgents.add(hostName);
              }
            }
          }
          sendAlarm(removed);
        } catch (Exception e) {
          LOG.error(e.getMessage(), e);
        }
      }
    }
  }
}
