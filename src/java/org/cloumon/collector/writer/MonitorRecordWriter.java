package org.cloumon.collector.writer;

import java.io.IOException;

import org.cloumon.thrift.MetricRecord;

import com.gruter.common.conf.GruterConf;

public abstract class MonitorRecordWriter {
  private GruterConf conf;
  public MonitorRecordWriter(GruterConf conf) throws IOException {
    this.conf = conf;
  }
  public abstract void write(MetricRecord monitorRecord) throws IOException;
}
