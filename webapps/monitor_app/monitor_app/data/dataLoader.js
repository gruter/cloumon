include('../../monitor_app.js');

var MonitorDataLoader = function() {
};

MonitorDataLoader.prototype = {
  //Host 목록을 가져온다.
  getHosts: function(callback) {
    uki.ajax({
      url: '/monitor?action=GetHosts',
      async: false,
      success: function(data) {
        if(data == '' || data == undefined) {
          data = [];
        }
        callback(data);
      }
    }); 
  },
  //Item이 배포되어 있는 Host 목록을 가져온다.
  getMonitorItemHosts: function(itemId, callback) {
    uki.ajax({
      url: '/monitor?action=GetMonitorItemHosts&itemId=' + itemId,
      async: false,
      success: function(data) {
        if(data == '' || data == undefined) {
          data = [];
        }
        callback(data);
      }
    }); 
  },
  //전체 Item 목록을 가져온다.
  getMonitorItems: function(callback) {
    uki.ajax({
      url: '/monitor?action=GetMonitorItems',
      async: false,
      success: function(data) {
        if(data == '' || data == undefined) {
          data = [];
        }
        callback(data);
      }
    });  
  },
  //특정 Item 목록 가져온다.
  getMonitorItem: function(itemId, callback) {
    uki.ajax({
      url: '/monitor?action=GetMonitorItem&itemId=' + itemId,
      async: false,
      success: function(data) {
        callback(data);
      }
    });  
  },
  //호스트의 알람항목을 수정한다.
  modifyHostAlarm: function(hostName, hostAlarm, alarmOn, callback) {
    var param = "action=ModifyHostAlarm";
    param += "&hostName=" + hostName;
    param += "&hostAlarm=" + hostAlarm;
    param += "&alarmOn=" + alarmOn;

    uki.ajax({
      url:  '/monitor',
      async: false,
      type: 'POST',
      data: param,
      success: function(data) {
        callback(data);
      }
    });     
  }, 
  //Item을 추가한다.
  addMonitorItem: function( itemId, itemName, itemGroup, adaptor, defaultItem, period, 
                            params, description, alarmExpr, occurTimes, alarmTo, 
                            callback) {
      var param = "action=AddMonitorItem";
      param += "&itemName=" + itemName;
      param += "&itemGroup=" + itemGroup;
      param += "&adaptor=" + adaptor;
      param += "&defaultItem=" + defaultItem;
      param += "&period=" + period;
      param += "&params=" + params;
      param += "&description=" + description;
      param += "&alarmExpr=" + alarmExpr;
      param += "&occurTimes=" + occurTimes;
      param += "&alarmTo=" + alarmTo;

      //alert(param);
      uki.ajax({
        url:  '/monitor',
        async: false,
        type: 'POST',
        data: param,
        success: function(data) {
          if(data != 'success') {
            alert('fail: ' + data);
          } else {
            callback(data);
          }
        }
      });    
  },
  //Item을 수정한다.
  modifyMonitorItem: function( itemId, period, params, description, alarmExpr, occurTimes, alarmTo, autoDeployAlarm, callback) {
      var param = "action=ModifyMonitorItem";
      param += "&itemId=" + itemId;
      param += "&period=" + period;
      param += "&params=" + params;
      param += "&description=" + description;
      param += "&alarmExpr=" + alarmExpr;
      param += "&occurTimes=" + occurTimes;
      param += "&alarmTo=" + alarmTo;
      param += "&autoDeployAlarm=" + autoDeployAlarm;
      
      //alert(param);
      uki.ajax({
        url:  '/monitor',
        async: false,
        type: 'POST',
        data: param,
        success: function(data) {
          if(data != 'success') {
            alert('fail: ' + data);
          } else {
            callback(data);
          }
        }
      });    
  },  
  //Host의 할당된 Item의 정보(알람정보)를 수정한다.  
  modifyHostMonitorItem: function( itemId, hostName, alarmExpr, occurTimes, alarmTo, callback) {
      var param = "action=ModifyHostMonitorItem";
      param += "&itemId=" + itemId;
      param += "&hostName=" + hostName;
      param += "&alarmExpr=" + alarmExpr;
      param += "&occurTimes=" + occurTimes;
      param += "&alarmTo=" + alarmTo;
      
      //alert(param);
      uki.ajax({
        url:  '/monitor',
        async: false,
        type: 'POST',
        data: param,
        success: function(data) {
          if(data != 'success') {
            alert('fail: ' + data);
          } else {
            callback(data);
          }
        }
      });    
  }, 
  //Item을 삭제한다.
  deleteMonitorItem: function(itemId, callback) {
    uki.ajax({
      url: '/monitor?action=DeleteMonitorItem&itemId=' + itemId,
      async: false,
      success: function(data) {
        callback(data);
      }
    });    
  },
  //Host에 Item을 추가한다.
  addItemToHost: function(itemId, hosts, callback) {
    var hostsParam = '';
    uki.each(hosts, function(i, value) {
      hostsParam += value + ',';
    });
    
    if(hostsParam == '') {
      if(!confirm('remove items from all hosts')) {
        return;
      }
    }
    var param = "action=AddMonitorItemToHost&itemId=" + itemId + "&hosts=" + hostsParam;
    uki.ajax({
      url:  '/monitor',
      async: false,
      type: 'POST',
      data: param,
      success: function(data) {
        if(data != 'success') {
          alert('fail: ' + data);
        } else {
          if(callback != undefined) {
            callback(data);
          }
        }
      }
    });      
  },
  //Host의 ItemGroup명을 가져온다.
  getHostItemGroup: function(hostName, callback) {
    uki.ajax({
      url: '/monitor?action=GetHostItemGroup&hostName=' + hostName,
      async: false,
      success: function(data) {
        if(data == '' || data == undefined) {
          data = [];
        }
        callback(data);
      }
    });
  },            
  //전체 Host의 Metric 요약 정보를 가져온다. 
  getHostSummaryMetrics: function(callback) {
    uki.ajax({
      url: '/monitor?action=GetHostSummaryMetrics',
      async: false,
      success: function(data) {
        if(data == '' || data == undefined) {
          data = [];
        }      
        callback(data);
      }
    });
  },
  //특정 Host의 가장 최근 Metrics 데이터를 가져온다.
  getHostCurrentMetrics: function(hostName, groupName, callback) {
    uki.ajax({
      url: '/monitor?action=GetHostCurrentMetrics&hostName=' + hostName + '&groupName=' + groupName,
      async: false,
      success: function(data) {
        if(data == '' || data == undefined) {
          data = [];
        }      
        callback(data);
      }
    });
  },
  //특정 Host의 특정 ItemGroup에 대한 기간별 데이터를 가져온다.
  getHostHistoryMetrics: function(hostName, groupName, itemNames, startTime, endTime, callback) {
    var param = "&hostName=" + hostName;
    param += "&groupName=" + groupName;
    param += "&startTime=" + startTime;
    param += "&endTime=" + endTime;
    param += "&items=" + itemNames;
      
    uki.ajax({
      url: '/monitor?action=GetHostHistoryMetrics' + param,
      async: false,
      success: function(data) {
        if(data == '' || data == undefined) {
          data = [];
        }  
        callback(data);
      }
    });
  },
  getServiceGroup: function(callback) {
    uki.ajax({
      url:  '/monitor?action=GetServiceGroup',
      async: false,
      success: function(data) {
        if(data == '' || data == undefined) {
          data = [];
        }      
        callback(data);
      }
    });
  },  
  getServiceGroupAsArray: function(callback) {
    uki.ajax({
      url:  '/monitor?action=GetServiceGroup',
      async: false,
      success: function(data) {
        if(data == '' || data == undefined) {
          data = [];
        }      
        var groups = new Array();
        uki.each(eval(data), function(index, value) {
          uki.each(value, function(key, value1) {
            groups[index] = value1;
          });
        });
        callback(groups);     
      }
    });  
  },
  //서비스에 등록된 Host 목록을 가져온다.        
  getServiceHosts: function(serviceGroupName, callback) {
    uki.ajax({
      url: '/monitor?action=GetServiceHosts&serviceGroupName=' + serviceGroupName,
      async: false,
      success: function(data) {
        if(data == '' || data == undefined) {
          data = [];
        }
        callback(data);
      }
    });    
  },
  //Service를 등록한다.
  addServiceGroup: function(serviceGroupName, callback) {
    var param = "action=AddServiceGroup&serviceGroupName=" + serviceGroupName;
    uki.ajax({
      url:  '/monitor',
      async: false,
      type: 'POST',
      data: param,
      success: function(data) {
        if(data != 'success') {
          alert('fail: ' + data);
        } else {
          callback(data);
        }
      }
    });    
  },
  //Service에 Host를 등록한다.
  addHostsToService: function(serviceGroupName, hosts, callback) {
    var hostsParam = '';
    uki.each(hosts, function(i, value) {
      hostsParam += value + ',';
    });
    
    var param = "action=AddServiceGroupHost&serviceGroupName=" + serviceGroupName + "&hosts=" + hostsParam;
    uki.ajax({
      url:  '/monitor',
      async: false,
      type: 'POST',
      data: param,
      success: function(data) {
        if(data != 'success') {
          alert('fail: ' + data);
        } else {
          if(callback != undefined) {
            callback(data);
          }
        }
      }
    });      
  },
  deleteHosts: function(hostNames, callback) {
    uki.ajax({
      url:  '/monitor?action=DeleteHosts&hostNames=' + hostNames,
      async: false,
      success: function(data) {  
        callback(data);
      }
    });
  },  
  deleteServiceGroup: function(serviceGroupName, callback) {
    uki.ajax({
      url:  '/monitor?action=DeleteServiceGroup&serviceGroupName=' + serviceGroupName,
      async: false,
      success: function(data) {  
        callback(data);
      }
    });
  },
  deleteServiceGroupHosts: function(serviceGroupName, hostNames, callback) {
    uki.ajax({
      url:  '/monitor?action=DeleteServiceGroupHosts&serviceGroupName=' + serviceGroupName + '&hostNames=' + hostNames,
      async: false,
      success: function(data) {  
        callback(data);
      }
    });
  },
  //히스토리 정보를 위해 시간 제한 값을 확인한다.
  checkMetricHistoryTime: function(startTime, endTime, callback) {
    uki.ajax({
      url:  '/monitor?action=CheckMetricHistoryTime&startTime=' + startTime + '&endTime='+ endTime,
      async: false,
      success: function(data) {  
        callback(data);
      }
    });
  },
  //DataNode 목록을 가져온다.        
  getDataNodeHosts: function(callback) {
    uki.ajax({
      url: '/monitor?action=GetDataNodeHosts',
      async: false,
      success: function(data) {
        if(data == '' || data == undefined) {
          data = [];
        }
        callback(data);
      }
    });    
  }  
};
