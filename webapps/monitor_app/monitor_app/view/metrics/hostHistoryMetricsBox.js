include('../../view.js');

uki.view.declare('monitor_app.view.HostHistoryMetricsBox', uki.view.Box, function(Base) {
	var mainBox;
	
	this._createDom = function() {
    Base._createDom.call(this);
    var rect = this.rect().clone();
    rect.height = 30;
    
    var tableRect = this.rect().clone();
    tableRect.y = 230 + 30;
    tableRect.height = 705 - 230;
        
    var currentDate = new Date();
    var startDate = $.format.date(currentDate, "yyyy-MM-dd 00:00");
    var endDate = $.format.date(currentDate, "yyyy-MM-dd 04:00");
    
    this.mainBox = uki.build( 
        { view: 'Box', rect: '0 0 800 735', anchors: 'left top right bottom',
          childViews: [
            { view: 'Box', rect: '0 0 800 30', anchors: 'left top right bottom',
              childViews: [ { view: 'TextField', rect: '10 5 120 22', anchors: 'left top', value: startDate, maxlength:16, id: 'hostHistoryMetricBoxStartTime' },
                  { view: 'Label', text: '~', rect: '135 5 15 22', anchors: 'left top'},
                  { view: 'TextField', rect: '150 5 120 22', anchors: 'left top', value: endDate, maxlength:16, id: 'hostHistoryMetricBoxEndTime' },
                  { view: 'Select', rect: '280 5 200 22', anchors: 'left top', rowHeight: 22, id: 'hostHistoryMetricBoxGroupName',
                    options: [{ value: '0', text: 'option 0'}] },
                  { view: 'Button', text: 'Query', rect: '485 5 50 22', anchors: 'left top', id: 'hostHistoryQueryButton'} ] 
            },
            { view: 'Box', rect: '0 30 800 230', anchors: 'left top',
              childViews: [
                {view: 'uki.view.NativeIframe', rect: '0 0 800 230', anchors: 'left top right bottom', id:'itemChartFrame', name:'itemChartFrame', src:'/item_chart.html'}
              ]
            },
            { view: 'Table', id: 'hostHistoryMetricsTable', rect: tableRect, anchors: 'left top right bottom', 
              multiselect: true, style: {fontSize: '12px', lineHeight: '12px'},
              columns: [
                { view: 'table.Column', label: 'time', key:'logTime', resizable: true, width: 100},
                { view: 'table.Column', label: '', key:'resource', resizable: true, width: 200 },
                { view: 'table.NumberColumn', label: '', key:'data', resizable: true, minWidth: 100, width: 100}
              ]
            }
          ]                 
        })[0];
            
    this.appendChild(this.mainBox);
    
    uki('#hostHistoryQueryButton').bind('click', function() {
      getHistoryMetrics();
    });     
	};

  getHistoryMetrics = function() {
    checkMetricHistoryTime();
  };
  
  checkMetricHistoryTime = function() {
    dataLoader.checkMetricHistoryTime(uki('#hostHistoryMetricBoxStartTime').value(), 
      uki('#hostHistoryMetricBoxEndTime').value(),
      function(data) {
        uki('#hostHistoryMetricBoxEndTime').value(data);
        getHistoryMetrics2();
      }
    );
  };
  
  getHistoryMetrics2 = function() {
    var indexes = uki('#hostSummaryMetricsTable').selectedIndexes();
    if(indexes.length != 1) {
      return;
    }
    var row = uki('#hostSummaryMetricsTable').selectedRows()[0];
    if (!row) return;

    //clear previous data
    var hostHistoryMetricsTableLength =uki('#hostHistoryMetricsTable').data().length;
    for(var i = 0; i < hostHistoryMetricsTableLength; i++) {
      uki('#hostHistoryMetricsTable').removeRow(0);
    }
      
    var hostName = row.hostName;
    var groupName = uki('#hostHistoryMetricBoxGroupName').value();
    var startTime = uki('#hostHistoryMetricBoxStartTime').value();
    var endTime = uki('#hostHistoryMetricBoxEndTime').value();
    
    //var param = "&hostName=" + row.hostName;
    //param += "&groupName=" + uki('#hostHistoryMetricBoxGroupName').value();
    //param += "&startTime=" + uki('#hostHistoryMetricBoxStartTime').value();
    //param += "&endTime=" + uki('#hostHistoryMetricBoxEndTime').value();
    
    dataLoader.getHostHistoryMetrics(hostName, groupName, '', startTime, endTime,
      function(data) {
        var jsonData = eval(data); 
        if(jsonData.length == 0) {
          uki('#hostHistoryMetricsTable').columns([]);
          return;
        }        
        var columns = new Array();
        var dataKeys = new Array();
        for (var jsonName in jsonData[0] ) {
          dataKeys.push(jsonName);
        }

        var columnWidth = 800/dataKeys.length;
        var first = true;
        for (var i = 0; i < dataKeys.length; i++) {
          if( i== 0) {
            columns.push({ view: 'table.Column', label: dataKeys[i], key:dataKeys[i], resizable: true, width: columnWidth});
          } else {
            columns.push({ view: 'table.NumberColumn', label: dataKeys[i], key:dataKeys[i], resizable: true, width: columnWidth});
          }
          if(!first) {
            first = false;
          }
        };
        uki('#hostHistoryMetricsTable').columns(columns);
        uki('#hostHistoryMetricsTable').data(jsonData);
        
        window.frames['itemChartFrame'].showGraph(groupName, hostName, startTime, endTime, jsonData);
      }
    );
  };  	
});
