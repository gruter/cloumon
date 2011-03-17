package org.cloumon.collector.writer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloumon.thrift.MetricRecord;

import com.gruter.common.conf.GruterConf;

public class PipelineWriter extends MonitorRecordWriter {
  static final Log LOG = LogFactory.getLog(PipelineWriter.class);
  private List<MonitorRecordWriter> writers = new ArrayList<MonitorRecordWriter>();

  public PipelineWriter(GruterConf conf) throws IOException {
    super(conf);
    String writerClassProperty = conf.get("collector.writer.PipelineWriter.writers", 
        "org.cloumon.collector.writer.DBWriter,org.cloumon.collector.writer.HadoopWriter");
    
    String[] writerClasses = writerClassProperty.split(",");
    
    try {
      for (String eachClass: writerClasses) {
        Class writerClass = Class.forName(eachClass);
        Constructor constructor = writerClass.getConstructor(GruterConf.class);
        MonitorRecordWriter writer = (MonitorRecordWriter)constructor.newInstance(conf);
        LOG.info("Register writer: " + writer);
        writers.add(writer);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException(e.getMessage(), e);
    }
  }
  
  @Override
  public void write(MetricRecord metricsRecord) throws IOException {
    if (writers == null) {
      return;
    }
    
    Exception exception = null;
    
    int errorCount = 0;
    for (MonitorRecordWriter writer: writers) {
      try {
        writer.write(metricsRecord);
      } catch (Exception e) {
        LOG.error(writer.getClass() + "," + e.getMessage(), e);
        errorCount ++;
        exception = e;
      }
    }
    
    if (errorCount == writers.size()) {
      throw new IOException(exception.getMessage(), exception);
    }
  }
}
