package org.cloumon.manager.servlet.action;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GetHostItemGroupAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String hostName = request.getParameter("hostName");
      if(hostName == null || hostName.length() == 0) {
        throw new IOException("No hostName parameter");
      }
      List<String> itemGroups = MonitorManagerServer.getMonitorService().findHostItemGroup(hostName);

      if(itemGroups == null || itemGroups.isEmpty()) {
        return "";
      }
      
      JSONArray jsonArray = new JSONArray();
      for (String eachGroupName: itemGroups) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("groupName", eachGroupName);
        jsonArray.put(jsonObject);
      }
      
      return jsonArray.toString();
    } catch (TException e) {
      throw new IOException(e.getMessage(), e);
    } catch (JSONException e) {
      e.printStackTrace();
      return "";
    }
  }
}
