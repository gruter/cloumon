package org.cloumon.agent.item.server;

import org.cloumon.agent.item.MetricValue;

public class UptimeMetrics extends MetricValue {
  private double loadAverage1;
  private double loadAverage2;
  private double loadAverage3;
  
  private double uptime;
  private String uptimeString;
  
  public double getLoadAverage1() {
    return loadAverage1;
  }

  public void setLoadAverage1(double loadAverage1) {
    this.loadAverage1 = loadAverage1;
  }

  public double getLoadAverage2() {
    return loadAverage2;
  }

  public void setLoadAverage2(double loadAverage2) {
    this.loadAverage2 = loadAverage2;
  }

  public double getLoadAverage3() {
    return loadAverage3;
  }

  public void setLoadAverage3(double loadAverage3) {
    this.loadAverage3 = loadAverage3;
  }

  public double getUptime() {
    return uptime;
  }

  public void setUptime(double uptime) {
    this.uptime = uptime;
  }

  public String getUptimeString() {
    return uptimeString;
  }

  public void setUptimeString(String uptimeString) {
    this.uptimeString = uptimeString;
  }
}
