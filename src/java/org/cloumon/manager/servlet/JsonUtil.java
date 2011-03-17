package org.cloumon.manager.servlet;

import java.util.List;
import java.util.Map;

import org.cloumon.thrift.HostMonitorItem;
import org.cloumon.thrift.ItemType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {
  public static String getJsonValue(Object obj) {
    String str = obj.toString();
    if((str.startsWith("{") && str.endsWith("}")) || (str.startsWith("[") && str.endsWith("]"))) {
      return str;
    } else {
      return "\"" + str + "\"";
    }
  }
  
  public static String listToJson(List<? extends Object> datas) {
    if(datas == null || datas.isEmpty()) {
      return "";
    }
    
    try {
      StringBuilder sb = new StringBuilder();
      
      sb.append("[");
      int size = datas.size();
      for (int i = 0; i < size; i++) {
        sb.append(datas.get(i).toString());
        if(i < size - 1) {
          sb.append(",");
        }
      }
      sb.append("]");
      return sb.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }
  
  
  public static String mapToJson(Map<? extends Object, ? extends Object> datas) {
    if(datas == null || datas.isEmpty()) {
      return "";
    }
    
    try {
      StringBuilder sb = new StringBuilder();
      
      sb.append("[");
      int size = datas.size();
      int index = 0;
      for(Map.Entry<? extends Object, ? extends Object> entry: datas.entrySet()) {
        sb.append("{\"").append(entry.getKey().toString()).append("\"");
        sb.append(":");
        String value = entry.getValue().toString();
        if(value.startsWith("{") && value.endsWith("}")) {
          sb.append(value);
        } else {
          sb.append("\"").append(value).append("\"");
        }
        sb.append("}");
        if(index < size - 1) {
          sb.append(",");
        }
        index++;
      }
      sb.append("]");
      return sb.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }
  
  public static String hostMonitorItemToJson(List<HostMonitorItem> monitorItems) {
    try {
      JSONArray jsonArray = new JSONArray();
      for (HostMonitorItem eachItem: monitorItems) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("itemType", eachItem.getItemType() == ItemType.HOST ? "HOST" : "DEFAULT");
        jsonObject.put("hostName", eachItem.getHostName());
        jsonObject.put("hostIp", eachItem.getHostIp());
        jsonObject.put("groupName", eachItem.getMonitorItem().getGroupName());
        jsonObject.put("itemId", eachItem.getMonitorItem().getItemId());
        jsonObject.put("itemName", eachItem.getMonitorItem().getItemName());
        jsonArray.put(jsonObject);
      }
      
      JSONObject dataObject = new JSONObject();
      dataObject.put("items", jsonArray);
      
      JSONObject resultObject = new JSONObject();
      resultObject.put("content", dataObject);
      resultObject.put("success", true);
      return resultObject.toString();
    } catch (JSONException e) {
      e.printStackTrace();
      return "";
    }
  }
}
