package org.cloumon.common.zk;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gruter.common.conf.GruterConf;
import com.gruter.common.zk.ZKUtil;


public class ZKPath {
  static final Log LOG = LogFactory.getLog(ZKPath.class);

  public static final String ZK_SERVICES_PATH = "/services";
  public static final String ZK_LIVE_SERVERS_PATH = "/live_servers";
  public static final String ZK_MASTER = "/master";

  public static final String ZK_MONITOR_SERVICE_NAME = "monitor";
  public static final String ZK_AGENT_SERVICE_NAME = "monitor_agent";
  public static final String ZK_COLLECTOR_SERVICE_NAME = "monitor_collector";

  public static final String ZK_MINITOR_EVENT_PATH = "/monitor_item_event";
  
  public static final String ZK_UNIQUE_KEY_PATH = "/unique_key";

  public static String getServiceMasterDir(GruterConf conf, String serviceName) {
    return ZKUtil.getServicePath(conf, serviceName) + ZK_MASTER;
  }

  public static String getServiceLiveServerPath(GruterConf conf, String serviceName) {
    return ZKUtil.getServicePath(conf, serviceName) + ZK_LIVE_SERVERS_PATH;
  }
}
