package org.cloumon.collector.writer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.cloumon.collector.dao.CollectorDAO;
import org.cloumon.collector.dao.CollectorDAODBImpl;
import org.cloumon.common.util.DBConnectionPool;
import org.cloumon.thrift.MetricRecord;

import com.gruter.common.conf.GruterConf;

public class DBWriter extends MonitorRecordWriter {
  public static DateFormat df = new SimpleDateFormat("yyyyMM");
  public static final String MONITOR_MASTER_DB_POOL_NAME = "monitor_master";
  private CollectorDAO collectorDAO;
  
  public DBWriter(GruterConf conf) throws IOException {
    super(conf);
    String dbDriver = conf.get("cloumon.db.driver");
    if (dbDriver == null || dbDriver.trim().length() == 0) {
      throw new IOException ("No cloumon.db.driver in conf");
    }

    String dbUri = conf.get("cloumon.db.uri");
    if (dbUri == null || dbUri.trim().length() == 0) {
      throw new IOException ("No cloumon.db.uri in conf");
    }

    DBConnectionPool.setupPool(MONITOR_MASTER_DB_POOL_NAME,  dbDriver, dbUri, null);
    
    collectorDAO = new CollectorDAODBImpl();
  }

  @Override
  public void write(MetricRecord metricsRecord) throws IOException {
    String tableName = getTableName(metricsRecord.getTimestamp());
    collectorDAO.addCurrentMetricsValue(metricsRecord);
    collectorDAO.addMetricsValue(tableName, metricsRecord);
  }
  
  private String getTableName(long logTime) {
    String logMonth = df.format(logTime);
    return "MetricRecord_" + logMonth;
  }
}
