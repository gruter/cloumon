package org.cloumon.agent.item.server;

import org.cloumon.agent.item.MetricValue;

public class NetworkMetrics extends MetricValue {
  private long txBytes;
  private long rxBytes;
  private long rxPackets;
  private long txPackets;
  
  public long getTxBytes() {
    return txBytes;
  }
  public void setTxBytes(long txBytes) {
    this.txBytes = txBytes;
  }
  public long getRxBytes() {
    return rxBytes;
  }
  public void setRxBytes(long rxBytes) {
    this.rxBytes = rxBytes;
  }
  public long getRxPackets() {
    return rxPackets;
  }
  public void setRxPackets(long rxPackets) {
    this.rxPackets = rxPackets;
  }
  public long getTxPackets() {
    return txPackets;
  }
  public void setTxPackets(long txPackets) {
    this.txPackets = txPackets;
  }
}
