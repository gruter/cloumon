package org.cloumon.agent.item.server;

import org.cloumon.agent.item.MetricValue;

public class DiskUsageMetrics extends MetricValue {
  private long size; 
  private long used;
  private long avail; 
  private double usePercent;
  private String mountedOn;
  private String type;
  public long getSize() {
    return size;
  }
  public void setSize(long size) {
    this.size = size;
  }
  public long getUsed() {
    return used;
  }
  public void setUsed(long used) {
    this.used = used;
  }
  public long getAvail() {
    return avail;
  }
  public void setAvail(long avail) {
    this.avail = avail;
  }
  public double getUsePercent() {
    return usePercent;
  }
  public void setUsePercent(double usePercent) {
    this.usePercent = usePercent;
  }
  public String getMountedOn() {
    return mountedOn;
  }
  public void setMountedOn(String mountedOn) {
    this.mountedOn = mountedOn;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
}
