package org.cloumon.collector.writer;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.cloumon.thrift.MetricRecord;

import com.gruter.common.util.StringUtils;


public class MetricRecordUtil {
  public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
  public static SimpleDateFormat viewDF = new SimpleDateFormat("MM-dd HH:mm:ss");
  
  public static String convertToLogFormat(MetricRecord metricRecord) {
    
    String logFormat = df.format(new Date()) + " "
      + metricRecord.getHostIp() + " " 
      + metricRecord.getHostName() + " " 
      + metricRecord.getItemId() + " " 
      + metricRecord.getItemName() + " " 
      + metricRecord.getGroupName() + " " 
      + StringUtils.ifNull(metricRecord.getResourceName(), "-") + " " 
      + df.format(new Date(metricRecord.getTimestamp())) + " "  
      + "\"" + new String(metricRecord.getMonitorData()) + "\"";
    
    return logFormat;
  }
}
