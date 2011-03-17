package org.cloumon.manager.model;

public class AlarmCondition {
  private int version = 1;
  
  /**
   * 검사 조건 
   */
  private String expression;
  
  /**
   * 동일 조건이 몇번 지속해서 만족할 경우 
   */
  private int occurTimes;

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public int getOccurTimes() {
    return occurTimes;
  }

  public void setOccurTimes(int occurTimes) {
    this.occurTimes = occurTimes;
  }
}
