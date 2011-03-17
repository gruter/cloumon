package org.cloumon.manager.model;

import java.util.List;

import org.cloumon.common.util.CollectionUtils;


public class Alarm {
  int version = 1;
  
  String hostName;
  
  String itemId;
  
  /**
   * 알람 조건(한 알람에 대해 여러 시간대별로 설정 가능)
   */
  List<AlarmCondition> alarmConditions;
  
  /**
   * 알람 대상(메일 또는 SMS 전화번호)
   */
  List<String> alarmTargets;

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public List<AlarmCondition> getAlarmConditions() {
    return alarmConditions;
  }

  public void setAlarmConditions(List<AlarmCondition> alarmConditions) {
    this.alarmConditions = alarmConditions;
  }

  public List<String> getAlarmTargets() {
    return alarmTargets;
  }

  public void setAlarmTargets(List<String> alarmTargets) {
    this.alarmTargets = alarmTargets;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\"alarmTo\":").append("\"").append(CollectionUtils.toString(alarmTargets)).append("\",");
    
    if(!alarmConditions.isEmpty()) {
      AlarmCondition condition = alarmConditions.get(0);
      sb.append("\"alarmExpr\":").append("\"").append(condition.getExpression()).append("\",");
      sb.append("\"occurTimes\":").append("\"").append(condition.getOccurTimes()).append("\"");
    }
    return sb.toString();
  }

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }
}
