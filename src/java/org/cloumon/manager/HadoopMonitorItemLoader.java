package org.cloumon.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.hdfs.protocol.FSConstants.DatanodeReportType;
import org.apache.thrift.TException;
import org.cloumon.agent.item.HadoopMetricAdaptor;
import org.cloumon.manager.alarm.AlarmManager;
import org.cloumon.thrift.MonitorItem;

import com.gruter.common.conf.GruterConf;

public class HadoopMonitorItemLoader {
  public static final Log LOG = LogFactory.getLog(HadoopMonitorItemLoader.class);
  
  static final String dfsSample = "AddBlockOps=0, CreateFileOps=0, DeleteFileOps=0, FileInfoOps=0, FilesAppended=0, " +
  "FilesCreated=0, FilesRenamed=0, GetBlockLocations=0, GetListingOps=0, " +
  "JournalTransactionsBatchedInSync=0, Syncs_avg_time=0, Syncs_num_ops=0, " +
  "Transactions_avg_time=0, Transactions_num_ops=0, blockReport_avg_time=29, " +
  "blockReport_num_ops=1, fsImageLoadTime=1275";

  static final String fsNameSystemSample = "BlockCapacity=256, BlocksTotal=123, CapacityRemainingGB=98, " +
  "CapacityTotalGB=298, CapacityUsedGB=0, CorruptBlocks=0, ExcessBlocks=0, " +
  "FilesTotal=398, MissingBlocks=0, PendingDeletionBlocks=0, PendingReplicationBlocks=0, " +
  "ScheduledReplicationBlocks=0, TotalLoad=1, UnderReplicatedBlocks=0";

  static final String datanodeSample = "blockChecksumOp_avg_time=0, blockChecksumOp_num_ops=0, blockReports_avg_time=82, " +
  "blockReports_num_ops=1, block_verification_failures=0, blocks_read=0, blocks_removed=0, " +
  "blocks_replicated=0, blocks_verified=0, blocks_written=0, bytes_read=0, bytes_written=0, " +
  "copyBlockOp_avg_time=0, copyBlockOp_num_ops=0, heartBeats_avg_time=7, heartBeats_num_ops=4, " +
  "readBlockOp_avg_time=0, readBlockOp_num_ops=0, readMetadataOp_avg_time=0, readMetadataOp_num_ops=0, " +
  "reads_from_local_client=0, reads_from_remote_client=0, replaceBlockOp_avg_time=0, replaceBlockOp_num_ops=0, " +
  "writeBlockOp_avg_time=0, writeBlockOp_num_ops=0, writes_from_local_client=0, writes_from_remote_client=0";

  static final String jvmSample = "gcCount=181, gcTimeMillis=11032, logError=0, logFatal=0, logInfo=0, logWarn=0, memHeapCommittedM=81.0625, " +
  "memHeapUsedM=4.394356, memNonHeapCommittedM=23.191406, memNonHeapUsedM=19.346619, " +
  "threadsBlocked=0, threadsNew=0, threadsRunnable=6, threadsTerminated=0, " +
  "threadsTimedWaiting=9, threadsWaiting=13";  

  List<MonitorItem> dataNodeMonitorItems = new ArrayList<MonitorItem>();
  List<String> dataNodeMonitorItemIds = new ArrayList<String>();
  List<MonitorItem> nameNodeMonitorItems = new ArrayList<MonitorItem>();
  List<String> nameNodeMonitorItemIds = new ArrayList<String>();
  
  GruterConf conf;
  String fsLogPath;
  String jvmLogPath;
  MonitorManagerServer monitorManagerServer;

  List<String> liveDataNodes = new ArrayList<String>();
  List<String> deadDataNodes = new ArrayList<String>();
  
  CheckDataNodeThread checkDataNodeThread = new CheckDataNodeThread();
  
  String nameNodeHost;
  
  boolean dfsAdminUser = true;
  
  public HadoopMonitorItemLoader(GruterConf conf, MonitorManagerServer monitorManagerServer) throws IOException {
    this.conf = conf;
    this.monitorManagerServer = monitorManagerServer;
    this.fsLogPath = conf.get("hdfs.metrics.path");
    this.jvmLogPath = conf.get("hadoop.jvm.metrics.path");
  }
  
  public static String getHostName(String hostPort) {
    int index = hostPort.indexOf(":");
    if(index < 0) {
      return hostPort;
    }
    return hostPort.substring(0, index);
  }
  
  public static int getPort(String hostPort) {
    int index = hostPort.indexOf(":");
    if(index < 0) {
      return 0;
    }
    return Integer.parseInt(hostPort.substring(index+1));    
  }
  
  public List<String> getLiveDataNodes() {
    return liveDataNodes;
  }
  
  public List<String> getDeadDataNodes() {
    return deadDataNodes;
  }
  
  public String getNameNodeHost() {
    return nameNodeHost;
  }
  
  private MonitorItem getHadoopMonitorItem(String itemTokenStr, String serverType, int itemId, String itemKey, String params) {
    String[] itemTokens = itemTokenStr.trim().split("=");
    if(itemTokens[0].equals("hostName") || itemTokens[0].equals("sessionId") || itemTokens[0].equals("processName")) {
      return null;
    }
    MonitorItem monitorItem = new MonitorItem();
    monitorItem.setAdaptorClass(HadoopMetricAdaptor.class.getName());
    monitorItem.setDefaultItem(false);
    monitorItem.setPeriod(60);
    monitorItem.setDescription("hdfs " + serverType);
    monitorItem.setGroupName(serverType);
    monitorItem.setItemId(serverType + itemId);
    monitorItem.setItemName(itemTokens[0]);
    monitorItem.setAlarm(AlarmManager.getAlaramDBStr("$value < 0", "0", conf.get("alarm.mail.to", "test@test.com")));
    String logFilePath = fsLogPath;
    if(itemKey.equals("jvm.metrics")) {
      logFilePath = jvmLogPath;
    }
    String allParams = logFilePath + " " + itemKey + " " + itemTokens[0];
    if(params != null) {
      allParams += " " + params;
    }
    monitorItem.setParams(allParams);
    return monitorItem;
  }
  
  protected void loadHadoopItems() throws IOException {
    String fsName = conf.get("fs.default.name");
    if(fsName == null || fsName.trim().length() == 0) {
      return;
    }
    
    Configuration hadoopConf = new Configuration();
    hadoopConf.set("fs.default.name", fsName);
    FileSystem fs = FileSystem.get(hadoopConf);
    
    nameNodeHost = ipToHostname(fs.getUri().getHost());
    try {
      List<MonitorItem> savedNameNodeMonitorItems = monitorManagerServer.monitorService.findMonitorItemByGroup("namenode");
      if(savedNameNodeMonitorItems != null && !savedNameNodeMonitorItems.isEmpty()) {
        nameNodeMonitorItems = savedNameNodeMonitorItems;
      }
      List<MonitorItem> savedDataNodeMonitorItems = monitorManagerServer.monitorService.findMonitorItemByGroup("datanode");
      if(savedDataNodeMonitorItems != null && !savedDataNodeMonitorItems.isEmpty()) {
        dataNodeMonitorItems = savedDataNodeMonitorItems;
      }
    } catch (TException e) {
      LOG.error(e.getMessage(), e);
      throw new IOException(e.getMessage(), e);
    }
    
    if(nameNodeMonitorItems.isEmpty()) {
      int itemId = 1;
      //NameNode
      String[] tokens = dfsSample.split(",");
      
      for(String eachToken: tokens) {
        MonitorItem monitorItem = getHadoopMonitorItem(eachToken, "namenode", itemId++, "dfs.namenode", null);
        if(monitorItem != null) {
          nameNodeMonitorItems.add(monitorItem);
        }
      }
      
      tokens = fsNameSystemSample.split(",");
      for(String eachToken: tokens) {
        MonitorItem monitorItem = getHadoopMonitorItem(eachToken, "namenode", itemId++, "dfs.FSNamesystem", null);
        if(monitorItem != null) {
          nameNodeMonitorItems.add(monitorItem);
        }
      }
      
      //DataNode
      tokens = datanodeSample.split(",");
      for(String eachToken: tokens) {
        MonitorItem monitorItem = getHadoopMonitorItem(eachToken, "datanode", itemId++, "dfs.datanode", null);
        if(monitorItem != null) {
          dataNodeMonitorItems.add(monitorItem);
        }
      }
      
      //jvm
      tokens = jvmSample.split(",");
      for(String eachToken: tokens) {
        MonitorItem namenodeItem = getHadoopMonitorItem(eachToken, "namenode", itemId++, "jvm.metrics", "processName=NameNode");
        if(namenodeItem != null) {
          nameNodeMonitorItems.add(namenodeItem);
        }
        MonitorItem datanodeItem = getHadoopMonitorItem(eachToken, "datanode", itemId++, "jvm.metrics", "processName=DataNode");
        if(datanodeItem != null) {
          dataNodeMonitorItems.add(datanodeItem);
        }
      }
    }
    
    for(MonitorItem eachItem: dataNodeMonitorItems) {
      dataNodeMonitorItemIds.add(eachItem.getItemId());
    }
    
    for(MonitorItem eachItem: nameNodeMonitorItems) {
      nameNodeMonitorItemIds.add(eachItem.getItemId());
    }
  }
  
  protected void saveMonitorItems() throws IOException {
    try {
      monitorManagerServer.monitorService.addMinitorItem(nameNodeMonitorItems);
      monitorManagerServer.monitorService.addMinitorItem(dataNodeMonitorItems);
    } catch (Exception e) {
      throw new IOException(e.getMessage(), e);
    }
  }
  
  protected void checkDataNode() {
    checkDataNodeThread.start();
  }
  
  class CheckDataNodeThread extends Thread {
    @Override
    public void run() {
      while(true) {
        try {
          checkDataNodeStatus();
        } catch (Exception e) {
          LOG.error(e.getMessage(), e);
        }
        try {
          Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
        }  
      }
    }
    
    protected void checkDataNodeStatus() throws IOException {
      String fsName = conf.get("fs.default.name");
      Configuration hadoopConf = new Configuration();
      hadoopConf.set("fs.default.name", fsName);
      
      FileSystem fs = FileSystem.get(hadoopConf);
      
      nameNodeHost = ipToHostname(fs.getUri().getHost());
      DistributedFileSystem dfs = (DistributedFileSystem) fs;
      String[] liveNodes = null;
      String[] deadNodes = null;
      if(dfsAdminUser) {
        try {
          DatanodeInfo[] liveNodeInfos = dfs.getClient().datanodeReport(DatanodeReportType.LIVE);
          if(liveNodeInfos != null) {
            liveNodes = new String[liveNodeInfos.length];
            for(int i = 0; i < liveNodeInfos.length; i++) {
              liveNodes[i] = liveNodeInfos[i].getHostName();
            }
          }
          DatanodeInfo[] deadNodeInfos = dfs.getClient().datanodeReport(DatanodeReportType.DEAD);
          if(deadNodeInfos != null) {
            deadNodes = new String[deadNodeInfos.length];
            for(int i = 0; i < deadNodeInfos.length; i++) {
              deadNodes[i] = deadNodeInfos[i].getHostName();
            }
          }
        } catch (Exception e) {
          LOG.warn(e.getMessage() + ", use cloumon property ");
          dfsAdminUser = false;
        }
      }
      
      if(!dfsAdminUser) {
        String dataNodeFileName = conf.get("fs.datanode.file");
        if(dataNodeFileName != null && dataNodeFileName.trim().length() > 0) {
          liveNodes = loadDataNodeFromFile(dataNodeFileName);
        }
      }
      
//      LOG.info("nameNodeHost>>>" + nameNodeHost);
      synchronized(liveDataNodes) {
        addNameNodeItem(nameNodeHost);
        
        liveDataNodes.clear();
        if(liveNodes != null) {
          CollectionUtils.addAll(liveDataNodes, liveNodes);
        }
        deadDataNodes.clear();
        if(deadNodes != null) {
          CollectionUtils.addAll(deadDataNodes, deadNodes);
        }
        for(String eachDatanode: liveDataNodes) {
          String hostName = ipToHostname(eachDatanode);
//          LOG.info("Live DataNode>>>" + hostName + ">" + dataNodeMonitorItemIds.size());
          addDataNodeItem(hostName);
        }
        for(String eachDatanode: deadDataNodes) {
          String hostName = ipToHostname(eachDatanode);
          removeDataNodeItem(hostName);
        }
      }
    }
    
    private String[] loadDataNodeFromFile(String dataNodeFileName) {
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new FileReader(new File(dataNodeFileName)));
        String line = null;
        List<String> dataNodes = new ArrayList<String>();
        while( (line = reader.readLine()) != null) {
          if(line.trim().length() > 1) {
            dataNodes.add(line.trim());
          }
        }
        return dataNodes.toArray(new String[]{});
      } catch (Exception e) {
        return null;
      } finally {
        if(reader != null) {
          try {
            reader.close();
          } catch (IOException e) {
          }
        }
      }
    }

    protected void addNameNodeItem(String hostName) throws IOException {
      List<MonitorItem> oldNameNodeMonitorItems = monitorManagerServer.monitorService.findHostMonitorItemsByGroup(hostName, "nameNode");
      if(oldNameNodeMonitorItems == null || oldNameNodeMonitorItems.isEmpty()) {
        try {
          monitorManagerServer.monitorService.addHostToMonitorItem(nameNodeMonitorItemIds, Arrays.asList(new String[]{hostName}));
        } catch (TException e) {
          LOG.error(e.getMessage(), e);
        }
      }
    }
    
    protected void addDataNodeItem(String hostName) throws IOException {
      List<MonitorItem> oldDataNodeMonitorItems = monitorManagerServer.monitorService.findHostMonitorItemsByGroup(hostName, "datanode");
      if(oldDataNodeMonitorItems == null || oldDataNodeMonitorItems.isEmpty()) {
        try {
          monitorManagerServer.monitorService.addHostToMonitorItem(dataNodeMonitorItemIds, Arrays.asList(new String[]{hostName}));
        } catch (TException e) {
          LOG.error(e.getMessage(), e);
        }
      }
    }
    
    protected void removeDataNodeItem(String hostName) throws IOException {
      try {
        monitorManagerServer.monitorService.removeHostsFromServiceGroup("datanode", Arrays.asList(new String[]{hostName}));
      } catch (TException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }
  
  private String ipToHostname(String ipAddress) {
    InetAddress inetAddress = null;
    try {
      inetAddress = InetAddress.getByName(ipAddress);
      return inetAddress.getCanonicalHostName();
    } catch (UnknownHostException e) {
      LOG.warn("Unknown Host:" + ipAddress);
      return ipAddress;
    }
  }
}
