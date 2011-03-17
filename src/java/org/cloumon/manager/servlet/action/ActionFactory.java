package org.cloumon.manager.servlet.action;

import javax.servlet.http.HttpServletRequest;

import org.cloumon.manager.servlet.action.hadoop.GetDataNodeHostsAction;

public class ActionFactory {
  public static MonitorAction getAction(HttpServletRequest request) {
    String actionParam = request.getParameter("action");
    
    if("GetMonitorItems".equals(actionParam)) {
      return new GetMonitorItemsAction();
    } else if("GetMonitorItem".equals(actionParam)) {
        return new GetMonitorItemAction();
    } else if("AddMonitorItem".equals(actionParam)) {
      return new AddMonitorItemAction();
    } else if("ModifyMonitorItem".equals(actionParam)) {
      return new ModifyMonitorItemAction();
    } else if("ModifyHostMonitorItem".equals(actionParam)) {
      return new ModifyHostMonitorItemAction();
    } else if("DeleteMonitorItem".equals(actionParam)) {
      return new DeleteMonitorItemAction();    
    } else if("GetHostItemGroup".equals(actionParam)) {
      return new GetHostItemGroupAction();
    } else if("GetMonitorItemAdaptors".equals(actionParam)) {
      return new GetMonitorItemAdaptorsAction();
    } else if("AddMonitorItemToHost".equals(actionParam)) {
      return new AddMonitorItemToHostAction();
    } else if("GetMonitorItemHosts".equals(actionParam)) {
      return new GetMonitorItemHostsAction();
    } else if("GetHostSummaryMetrics".equals(actionParam)) {
      return new GetHostSummaryMetricsAction();
    } else if("GetHostCurrentMetrics".equals(actionParam)) {
      return new GetHostCurrentMetricsAction();
    } else if("GetHostHistoryMetrics".equals(actionParam)) {
      return new GetHostHistoryMetricsAction();
    } else if("GetHosts".equals(actionParam)) {
      return new GetHostsAction();
    } else if("GetServiceHosts".equals(actionParam)) {
      return new GetServiceHostsAction();
    } else if("GetServiceGroup".equals(actionParam)) {
      return new GetServiceGroupAction();
    } else if("AddServiceGroup".equals(actionParam)) {
      return new AddServiceGroupAction();
    } else if("DeleteServiceGroup".equals(actionParam)) {
      return new DeleteServiceGroupAction();
    } else if("AddServiceGroupHost".equals(actionParam)) {
    	return new AddServiceGroupHostAction();
    } else if("HostRealtimeMetrics".equals(actionParam)) {
      return new HostRealtimeMetricsAction();
    } else if("CheckMetricHistoryTime".equals(actionParam)) {
      return new CheckMetricHistoryTimeAction();   
    } else if("DeleteServiceGroupHosts".equals(actionParam)) {
      return new DeleteServiceGroupHostsAction();       
    } else if("DeleteHosts".equals(actionParam)) {
      return new DeleteHostsAction();       
    } else if("ModifyHostAlarm".equals(actionParam)) {
      return new ModifyHostAlarmAction();    
    } else if("GetDataNodeHosts".equals(actionParam)) {
      return new GetDataNodeHostsAction();          
    } else {
      return null;
    }
  }
}
