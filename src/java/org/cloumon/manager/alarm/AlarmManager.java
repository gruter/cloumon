package org.cloumon.manager.alarm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;
import org.cloumon.manager.MonitorServiceImpl;
import org.cloumon.manager.model.Alarm;
import org.cloumon.manager.model.AlarmCondition;
import org.cloumon.thrift.HostInfo;
import org.cloumon.thrift.MetricViewRecord;
import org.cloumon.thrift.MonitorItem;
import org.springframework.util.StringUtils;
import org.vedantatree.expressionoasis.ExpressionContext;
import org.vedantatree.expressionoasis.ExpressionEngine;

import com.gruter.common.conf.GruterConf;

public class AlarmManager implements Runnable {
  public static final Log LOG = LogFactory.getLog(AlarmManager.class);
  public static final int ALARM_VERSION = 1;
  
  //hostName_itemId -> occurTime;
  private Map<String, Integer> alarmOccurs = new HashMap<String, Integer>();
  
  //hostName_itemId -> logTime
  private Map<String, Long> lastMetricTimes = new HashMap<String, Long>();
  
  private Map<String, List<MetricViewRecord>> metricViewRecords = new HashMap<String, List<MetricViewRecord>>();
  
  private ExpressionContext expressionContext;
  
  private MonitorServiceImpl monitorService;
  
  private List<AlarmSender> alarmSenders;
  
  private GruterConf conf;

  
  public AlarmManager(GruterConf conf, MonitorServiceImpl monitorService) {
    this.conf = conf;
    this.monitorService = monitorService;
    this.alarmSenders = getSenders(conf);
  }
  
  public static List<AlarmSender> getSenders(GruterConf conf) {
    List<AlarmSender> alarmSenders = new ArrayList<AlarmSender>();
    try {
      String classNames = conf.get("alarm.sender", MailAlarmSender.class.getCanonicalName());
      String[] classTokens = classNames.split(",");
      for(String eachToken: classTokens) {
        alarmSenders.add((AlarmSender)Class.forName(eachToken).newInstance());
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return alarmSenders;
  }
  
  @Override
  public void run() {
    try {
      Thread.sleep(10 * 1000);
      expressionContext = new ExpressionContext();
    } catch (Exception e) {
      LOG.fatal(e.getMessage(), e);
    }

    while(true) {
      long startTime = System.currentTimeMillis();
      try {
        List<Alarm> alarms = monitorService.findAllHostAlarms();
        for(Alarm eachAlarm: alarms) {
          checkAlarm(eachAlarm);
        }
        long sleepTime = 60 * 1000 - (System.currentTimeMillis() - startTime);
        if(sleepTime >= 100) {
          Thread.sleep(sleepTime);
        }
        metricViewRecords.clear();
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
        System.exit(0);
      }
    }
  }
  
  private void checkAlarm(Alarm alarm) throws Exception {
    AlarmCondition alarmCondition = alarm.getAlarmConditions().get(0);
    
    List<MetricViewRecord> records = metricViewRecords.get(alarm.getHostName());
    if(records == null) {
      records = monitorService.getHostCurrentMetrics(alarm.getHostName(), null);
      metricViewRecords.put(alarm.getHostName(), records);
    } 

    String checkKey = alarm.getHostName() + alarm.getItemId();
    
    for(MetricViewRecord record: records) {
      if(record.getItemId().equals(alarm.getItemId())) {
        long lastCheckTime = 0;
        if(lastMetricTimes.containsKey(checkKey)) {
          lastCheckTime = lastMetricTimes.get(checkKey);
        } else {
          lastCheckTime = System.currentTimeMillis() - (120 * 1000);  //2min
        }
        lastMetricTimes.put(checkKey, record.getTimestamp());
        
        if(record.getTimestamp() <= lastCheckTime) {
          continue;
        }
        
        //value evaluation
        if(expressionContext == null) {
          continue;
        }
        
        String alarmExpr =  alarmCondition.getExpression().replace("$value", new String(record.getMonitorData()));
        Object exprResult = ExpressionEngine.evaluate(alarmExpr, expressionContext);
        if(exprResult instanceof Boolean) {
          if((Boolean)exprResult) {
            int occurTimes = alarmCondition.getOccurTimes();
            boolean alarmNoti = true;
            if(occurTimes > 0) {
              if(alarmOccurs.containsKey(checkKey) && alarmOccurs.get(checkKey) >= occurTimes) {
                alarmOccurs.remove(checkKey);
              } else {
                alarmOccurs.put(checkKey, alarmOccurs.containsKey(checkKey) ? alarmOccurs.get(checkKey) + 1 : 1);
                alarmNoti = false;
              }
            }
            
            if(alarmNoti) {
              HostInfo hostInfo = monitorService.getHostInfo(alarm.getHostName());
              MonitorItem monitorItem = monitorService.getMonitorItem(alarm.getItemId());
              for(AlarmSender eachSender: alarmSenders) {
                eachSender.sendAlarm(conf, alarm, hostInfo, monitorItem, alarmExpr, record.getMonitorData());
              }
            }
          }
        }
      }
    }
  }
  
  public static Alarm parseAlarm(String alarmStr) {
    Alarm alarm = new Alarm();
    
    String[] tokens = alarmStr.split(":");
    
    if(tokens.length < 4) {
      LOG.error("Wrong alarm format:" + alarmStr);
      return null;
    }
    
    AlarmCondition alarmCondition = new AlarmCondition();
    alarmCondition.setVersion(Integer.parseInt(tokens[0]));
    alarmCondition.setOccurTimes(Integer.parseInt(tokens[1]));
    String expr = "";
    for(int i = 3; i < tokens.length; i++) {
      expr += tokens[i] + " ";
    }
    alarmCondition.setExpression(expr);

    alarm.setAlarmConditions(Arrays.asList(new AlarmCondition[]{alarmCondition}));
    String alarmTargetTokens[] = tokens[2].split(",");
    
    alarm.setAlarmTargets(Arrays.asList(alarmTargetTokens));
    alarm.setVersion(Integer.parseInt(tokens[0]));
    
    return alarm;
  }
  
  public static String getAlaramDBStr(Alarm alarm) {
    AlarmCondition alarmCondition = alarm.getAlarmConditions().get(0);
    return alarm.getVersion() + ":" + alarmCondition.getOccurTimes() + ":" + 
              StringUtils.collectionToCommaDelimitedString(alarm.getAlarmTargets()) + ":" + alarmCondition.getExpression();
  }
  public static String getAlaramDBStr(String alarmExpr, String occurTimes, String alarmTo) {
    return ALARM_VERSION + ":" + occurTimes + ":" + alarmTo + ":" + alarmExpr;
  }
}
