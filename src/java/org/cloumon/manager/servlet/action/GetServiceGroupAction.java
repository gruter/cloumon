package org.cloumon.manager.servlet.action;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.cloumon.manager.MonitorManagerServer;
import org.cloumon.thrift.ServiceGroup;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetServiceGroupAction extends MonitorAction {
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      List<ServiceGroup> serviceGroups = MonitorManagerServer.getMonitorService().findAllServiceGroup();

      if(serviceGroups == null || serviceGroups.isEmpty()) {
        return "";
      }
      
      JSONArray jsonArray = new JSONArray();
      for (ServiceGroup eachService: serviceGroups) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(eachService.getServiceGroupName(), eachService.getServiceGroupName());
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
