package org.cloumon.agent.item.server;

import org.cloumon.agent.item.MetricValue;

public class MemoryMetrics extends MetricValue {
  private long used;
  private long actualUsed;
  private long freePercent;
  private long actualFree;
  private long ram;
  private long usedPercent;
  private long free;
  private long total;
  
  private long swapUsed;
  private long swapPageOut;
  private long swapPageIn;
  private long swapFree;
  private long swapTotal;
  
  public long getUsed() {
    return used;
  }
  public void setUsed(long used) {
    this.used = used;
  }
  public long getActualUsed() {
    return actualUsed;
  }
  public void setActualUsed(long actualUsed) {
    this.actualUsed = actualUsed;
  }
  public long getFreePercent() {
    return freePercent;
  }
  public void setFreePercent(long freePercent) {
    this.freePercent = freePercent;
  }
  public long getActualFree() {
    return actualFree;
  }
  public void setActualFree(long actualFree) {
    this.actualFree = actualFree;
  }
  public long getRam() {
    return ram;
  }
  public void setRam(long ram) {
    this.ram = ram;
  }
  public long getUsedPercent() {
    return usedPercent;
  }
  public void setUsedPercent(long usedPercent) {
    this.usedPercent = usedPercent;
  }
  public long getFree() {
    return free;
  }
  public void setFree(long free) {
    this.free = free;
  }
  public long getTotal() {
    return total;
  }
  public void setTotal(long total) {
    this.total = total;
  }
  public long getSwapUsed() {
    return swapUsed;
  }
  public void setSwapUsed(long swapUsed) {
    this.swapUsed = swapUsed;
  }
  public long getSwapPageOut() {
    return swapPageOut;
  }
  public void setSwapPageOut(long swapPageOut) {
    this.swapPageOut = swapPageOut;
  }
  public long getSwapPageIn() {
    return swapPageIn;
  }
  public void setSwapPageIn(long swapPageIn) {
    this.swapPageIn = swapPageIn;
  }
  public long getSwapFree() {
    return swapFree;
  }
  public void setSwapFree(long swapFree) {
    this.swapFree = swapFree;
  }
  public long getSwapTotal() {
    return swapTotal;
  }
  public void setSwapTotal(long swapTotal) {
    this.swapTotal = swapTotal;
  }
  
}
