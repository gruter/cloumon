package org.cloumon.collector.writer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.cloumon.thrift.MetricRecord;

import com.gruter.common.conf.GruterConf;

public class HadoopWriter extends MonitorRecordWriter {
  static Log LOG = LogFactory.getLog(HadoopWriter.class);
  
  private Path rootPath;
  private FileSystem fs;
  private FSDataOutputStream out;
  private Path currentPath;
  private Thread fileSyncAndRollingThread;
  private int currentHour;
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
  private String hostName;
  private Object outputMutex = new Object();
  
  public HadoopWriter(GruterConf conf) throws IOException {
    super(conf);
    this.rootPath = new Path(conf.get("collector.writer.HadoopWriter.archivePath", "/user/monitor/data"));
    
    String fsName = conf.get("collector.writer.HadoopWriter.hdfs.filesystem");
    Configuration hconf = new Configuration();
    if (fsName == null || fsName.equals("")) {
      fsName = hconf.get("fs.default.name");
    }
    try {
      this.fs = FileSystem.get(new URI(fsName), hconf);
    } catch (URISyntaxException e) {
      throw new IOException(e.getMessage(), e);
    }
    this.hostName = InetAddress.getLocalHost().getHostName();

    rolling();
    
    this.fileSyncAndRollingThread = new Thread(new FileSyncAndRolling(), "FileSyncAndRolling");
    this.fileSyncAndRollingThread.start();
  }

  @Override
  public void write(MetricRecord metricsRecord) throws IOException {
    synchronized(outputMutex) {
      out.write(1);
    }
  }

  private void rolling() throws IOException {
    if (out != null) {
      out.close();
    }
    String dateStr = dateFormat.format(new Date());
    Path currentParentPath = new Path(rootPath, dateStr.substring(0, 4) + "/" + dateStr.substring(4, 6) + "/" + dateStr.substring(6, 8));
    fs.mkdirs(currentParentPath);

    currentPath = new Path(currentParentPath, hostName + "_" + dateStr);
    currentHour = Integer.parseInt(dateStr.substring(8, 10));
    
    if (fs.exists(currentPath)) {
      Path seqPath = new Path(currentPath, "_" + System.currentTimeMillis());
      out = fs.create(seqPath);
      LOG.info("Metrics Log file created: " + seqPath);
    } else {
      out = fs.create(currentPath);
      LOG.info("Metrics Log file created: " + currentPath);
    }
  }
  
  class FileSyncAndRolling implements Runnable {
    @Override
    public void run() {
      while(true) {
        try {
          Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
        }
        
        try {
          synchronized(outputMutex) {
            if (currentHour != Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
              rolling();
            } else {
              out.sync();
            }
          }
        } catch (IOException e) {
          LOG.error(e.getMessage(), e);
        }
      }
    }
  }
}
