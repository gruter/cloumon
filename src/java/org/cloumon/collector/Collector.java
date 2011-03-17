package org.cloumon.collector;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.cloumon.collector.writer.MonitorRecordWriter;
import org.cloumon.common.zk.ZKPath;
import org.cloumon.thrift.CollectorService;
import org.cloumon.thrift.MetricRecord;

import com.gruter.common.conf.GruterConf;
import com.gruter.common.server.ThriftApplicationServer;
import com.gruter.common.util.StringUtils;
import com.gruter.common.zk.ZKKeyGen;

public class Collector extends ThriftApplicationServer implements CollectorService.Iface {
  static final Log LOG = LogFactory.getLog(Collector.class);

  public static final int DEFAULT_PORT = 9124;
  
  private MonitorRecordWriter writer;

  public Collector(GruterConf conf) throws IOException {
    super(conf);
    try {
      Class writerClass = Class.forName(
          conf.get("collector.writer", "org.cloumon.collector.writer.PipelineWriter"));
      Constructor constructor = writerClass.getConstructor(GruterConf.class);
      writer = (MonitorRecordWriter)constructor.newInstance(conf);
    } catch (Exception e) {
      throw new IOException(e.getMessage(), e);
    }
  }
  
  
  @Override
  public void addMetricRecord(List<MetricRecord> metricsRecords) throws TException {
    try {
      //DateFormat df = new SimpleDateFormat("mm-dd HH:mm:ss SSS");
      for (MetricRecord eachRecord: metricsRecords) {
        //LOG.info(eachRecord.getGroupName() + "." + eachRecord.getItemName() + "," + df.format(new Date(eachRecord.getTimestamp())));
        eachRecord.setRecordId("" + ZKKeyGen.getInstance(zk).getNextSequence("metric_record"));
        writer.write(eachRecord);
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new TException(e);
    } 
  }

  @Override
  protected TProcessor getProcessor() {
    return new CollectorService.Processor(this);
  }

  @Override
  public int getServerPort() {
    return conf.getInt("collector.port", DEFAULT_PORT);
  }

  @Override
  public String getServiceName() {
    return ZKPath.ZK_COLLECTOR_SERVICE_NAME;
  }

  public static void main(String[] args) {
//    args = new String[]{"7126"};
    try {
      StringUtils.startupShutdownMessage(Collector.class, args, LOG);
      GruterConf conf = new GruterConf();
      conf.addResource("cloumon-default.xml");
      conf.addResource("cloumon-site.xml");

      if (args.length > 0) {
        conf.set("collector.port", args[0]);
      }
      Collector collector = new Collector(conf);
      collector.startServer();
    } catch (Throwable e) {
      LOG.error(StringUtils.stringifyException(e));
      System.exit(-1);
    }
  }
}
