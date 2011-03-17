package org.cloumon.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.cloumon.thrift.MonitorItem;

public class MonitorItemUtil {
  private static void writeString(OutputStream out, String str) throws IOException {
    byte[] strBytes = str.getBytes();
    out.write(strBytes.length);
    out.write(strBytes);
  }

  private static String readString(InputStream in) throws IOException {
    byte[] data = new byte[in.read()];
    in.read(data);
    return new String(data);
  }

  public static byte[] getMonitorItemBytes(MonitorItem monitorItem) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    writeString(bout, monitorItem.getItemId());
    writeString(bout, monitorItem.getItemName());
    writeString(bout, monitorItem.getGroupName());
    writeString(bout, monitorItem.getAdaptorClass());
    bout.write(monitorItem.getPeriod());
    writeString(bout, monitorItem.getParams());
    writeString(bout, monitorItem.getDescription());

    return bout.toByteArray();
  }

  public static MonitorItem parseMonitorItem(byte[] data) throws IOException {
    ByteArrayInputStream bin = new ByteArrayInputStream(data);
    MonitorItem monitorItem = new MonitorItem();
    monitorItem.setItemId(readString(bin));
    monitorItem.setItemName(readString(bin));
    monitorItem.setGroupName(readString(bin));
    monitorItem.setAdaptorClass(readString(bin));
    monitorItem.setPeriod(bin.read());
    monitorItem.setParams(readString(bin));
    monitorItem.setDescription(readString(bin));
    
    return monitorItem;
  }
}
