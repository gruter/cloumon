package org.cloumon.agent.item;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloumon.agent.Agent;
import org.cloumon.thrift.MetricRecord;
import org.cloumon.thrift.MonitorItem;
import org.cloumon.util.FileHeadTail;

public class HadoopMetricAdaptor extends TimeEventAdaptor {
  static final Log LOG = LogFactory.getLog(HadoopMetricAdaptor.class);
  
//  dfs.namenode: hostName=hyungjoon-kim-ui-MacBook-Pro.local, sessionId=, AddBlockOps=0, CreateFileOps=0, DeleteFileOps=0, FileInfoOps=0, FilesAppended=0, FilesCreated=0, FilesRenamed=0, GetBlockLocations=0, GetListingOps=0, JournalTransactionsBatchedInSync=0, Syncs_avg_time=0, Syncs_num_ops=0, Transactions_avg_time=0, Transactions_num_ops=0, blockReport_avg_time=29, blockReport_num_ops=1, fsImageLoadTime=1275
//  dfs.FSDirectory: hostName=hyungjoon-kim-ui-MacBook-Pro.local, sessionId=, files_deleted=1043
//  dfs.FSNamesystem: hostName=hyungjoon-kim-ui-MacBook-Pro.local, sessionId=, BlockCapacity=256, BlocksTotal=123, CapacityRemainingGB=98, CapacityTotalGB=298, CapacityUsedGB=0, CorruptBlocks=0, ExcessBlocks=0, FilesTotal=398, MissingBlocks=0, PendingDeletionBlocks=0, PendingReplicationBlocks=0, ScheduledReplicationBlocks=0, TotalLoad=1, UnderReplicatedBlocks=0
//  dfs.datanode: hostName=hyungjoon-kim-ui-MacBook-Pro.local, sessionId=, blockChecksumOp_avg_time=0, blockChecksumOp_num_ops=0, blockReports_avg_time=82, blockReports_num_ops=1, block_verification_failures=0, blocks_read=0, blocks_removed=0, blocks_replicated=0, blocks_verified=0, blocks_written=0, bytes_read=0, bytes_written=0, copyBlockOp_avg_time=0, copyBlockOp_num_ops=0, heartBeats_avg_time=7, heartBeats_num_ops=4, readBlockOp_avg_time=0, readBlockOp_num_ops=0, readMetadataOp_avg_time=0, readMetadataOp_num_ops=0, reads_from_local_client=0, reads_from_remote_client=0, replaceBlockOp_avg_time=0, replaceBlockOp_num_ops=0, writeBlockOp_avg_time=0, writeBlockOp_num_ops=0, writes_from_local_client=0, writes_from_remote_client=0

//  jvm.metrics: hostName=hyungjoon-kim-ui-MacBook-Pro.local, processName=NameNode, sessionId=, gcCount=181, gcTimeMillis=11032, logError=0, logFatal=0, logInfo=0, logWarn=0, memHeapCommittedM=81.0625, memHeapUsedM=4.394356, memNonHeapCommittedM=23.191406, memNonHeapUsedM=19.346619, threadsBlocked=0, threadsNew=0, threadsRunnable=6, threadsTerminated=0, threadsTimedWaiting=9, threadsWaiting=13  
  public HadoopMetricAdaptor(Agent agent, MonitorItem monitorItem) {
    super(agent, monitorItem);
  }

  @Override
  public TimerTask getTimeTask() {
    return new HadoopMetricMonitorTask();
  }
  
  public boolean equals(Object obj) {
    if ( !(obj instanceof HadoopMetricAdaptor) ) {
      return false;
    }
    
    HadoopMetricAdaptor other = (HadoopMetricAdaptor)obj;
    
    return period == other.getPeriod();
  }
  
  class HadoopMetricMonitorTask extends TimerTask {
    @Override
    public void run() {
      synchronized(monitorItems) {
        long collectTime = System.currentTimeMillis();
        //LOG.info("start======================================");
        for (MonitorItem eachItem: monitorItems) {
          try {
            runEachItem(eachItem, collectTime);
          } catch (Exception e) {
            LOG.error(e.getMessage(), e);
          }
        }
        //LOG.info("end======================================");
      }
    }

    private void runEachItem(MonitorItem item, long collectTime) throws Exception {
      String[] params = item.getParams().split(" ");
      String fileName = params[0];
      String metricGroup = params[1];
      String metricItem = params[2];
      String filter = null;
      if(params.length > 3) {
        filter = params[3];
      }
      
      File file = new File(fileName);
      if(!file.exists() || file.isDirectory()) {
        return;
      }
      
      List<String> lines = FileHeadTail.tail(file, 5);
      for(String eachLine: lines) {
        if(eachLine.startsWith(metricGroup)) {
          if(filter != null && eachLine.indexOf(filter) < 0) {
            continue;
          }
          eachLine = eachLine.substring(eachLine.indexOf(":") + 1);
          String[] tokens = eachLine.split(",");
          for(String eachToken: tokens) {
            eachToken = eachToken.trim();
            String[] keyVal = eachToken.split("=");
            if(keyVal[0].equals(metricItem)) {
              MetricRecord metricsRecord = createMetricRecordObject(item);
              metricsRecord.setTimestamp(collectTime);
              metricsRecord.setMonitorData(ByteBuffer.wrap(keyVal[1].getBytes()));
              List<MetricRecord> metricsRecords = new ArrayList<MetricRecord>();
              metricsRecords.add(metricsRecord);
              
              agent.sendMonitorRecords(metricsRecords);
              return;
            }
          }
        }
      }
    }
  }
}
