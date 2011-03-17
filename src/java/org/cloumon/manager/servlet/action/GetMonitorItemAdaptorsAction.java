package org.cloumon.manager.servlet.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloumon.agent.item.ItemManager;
import org.cloumon.manager.servlet.JsonUtil;


public class GetMonitorItemAdaptorsAction extends MonitorAction {
  private static String adaptorJson = JsonUtil.mapToJson(ItemManager.adaptorClasses);
  
  @Override
  protected String doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    return adaptorJson;
  }
}
