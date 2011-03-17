package org.cloumon.collector.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.cloumon.thrift.MetricRecord;

import com.gruter.common.conf.GruterConf;

public class LocalFileWriter extends MonitorRecordWriter {
  static Log LOG = LogFactory.getLog(LocalFileWriter.class);
  
  private String rootPath;
  private FileSystem fs;
  private OutputStream out;
  private String currentPath;
  private Thread fileSyncAndRollingThread;
  private int currentHour;
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
  private String hostName;
  private Object outputMutex = new Object();
  
  public LocalFileWriter(GruterConf conf) throws IOException {
    super(conf);
    this.rootPath = conf.get("collector.writer.LocalFileWriter.localOutputDir", "/tmp/monitor/data");
    
    this.hostName = InetAddress.getLocalHost().getHostName();

    rolling();
    
    this.fileSyncAndRollingThread = new Thread(new FileSyncAndRolling(), "FileSyncAndRolling");
    this.fileSyncAndRollingThread.start();
  }

  @Override
  public void write(MetricRecord metricsRecord) throws IOException {
    synchronized(outputMutex) {
      out.write(MetricRecordUtil.convertToLogFormat(metricsRecord).getBytes());
      out.write("\n".getBytes());
    }
  }

  private void rolling() throws IOException {
    if (out != null) {
      out.close();
    }
    String dateStr = dateFormat.format(new Date());
    String currentParentPath = rootPath  + "/" 
        + dateStr.substring(0, 4) + "/" + dateStr.substring(4, 6) + "/" 
        + dateStr.substring(6, 8);
    
    File file = new File(currentParentPath);
    file.mkdirs();
    
    currentPath = currentParentPath + "/" + hostName + "_" + dateStr;
    currentHour = Integer.parseInt(dateStr.substring(8, 10));

    LOG.info("Metrics Log file created: " + currentPath);
    out = new FileOutputStream(currentPath, true);
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
            } 
          }
        } catch (IOException e) {
          LOG.error(e.getMessage(), e);
        }
      }
    }
  }
}
