include('../../view.js');

monitor_app.view.menuBox = {};

uki.view.declare('monitor_app.view.MetricsBox', uki.view.Box, function(Base) {
  var mainBox;
  
  this._createDom = function() {
    Base._createDom.call(this);
		mainBox = uki.build(
		  { view: 'VSplitPane', rect: '0 0 800 960', anchors: 'left top right bottom', handleWidth: 5, handlePosition: 195, minTop: 100,
        topChildViews: [ 
          { view: 'Box', rect: '0 0 800 30', anchors: 'left top  right bottom', 
            childViews: [
              { view: 'Button', rect: '710 5 80 22', anchors: 'right top', background: 'theme(field)', text: 'Refresh', id: 'metricSummaryRefreshButton' }
            ] 
          },
          { view: 'Box', rect: '0 30 800 170', anchors: 'left top right bottom', 
            childViews: [
              //server list table
              { view: 'Table', rect: '0 0 800 170', anchors: 'left top right bottom', id:'hostSummaryMetricsTable',
                style: { fontSize: '12px', lineHeight: '12px' }, multiselect: false,
                columns: [
                  { view: 'table.Column', label: 'Host Name', width: 150, minWidth: 50, resizable: true, key: 'hostName' },
                  { view: 'table.Column', label: 'Status', width: 50, resizable: true, key: 'liveStatus' },
                  { view: 'table.Column', label: 'Host Ip', width: 100, minWidth: 50, resizable: true, key: 'hostIp' },
                  { view: 'table.NumberColumn', label: 'Cpu Load', width: 80, minWidth: 50, resizable: true, key: 'cpuLoad' },
                  { view: 'table.NumberColumn', label: 'Cpu User', width: 80, minWidth: 50, resizable: true, key: 'cpuUser' },
                  { view: 'table.NumberColumn', label: 'Disk Used', width: 80, minWidth: 50, resizable: true, key: 'diskUsed' },
                  { view: 'table.NumberColumn', label: 'NetIn', width: 80, minWidth: 50, resizable: true, key: 'netIn' },
                  { view: 'table.NumberColumn', label: 'NetOut', width: 80, minWidth: 50, resizable: true, key: 'netOut' },
                  { view: 'table.Column', label: 'Time', width: 150, minWidth: 50, resizable: true, key: 'logTime' }
                ]
              }
            ]
          }
        ],
        bottomChildViews: [ 
          { view: 'monitor_app.view.TabPanel', rect: '0 0 800 760', anchors: 'left top right bottom',
            tabLables: [ { text: 'Current'}, { text: 'History'} ],
            tabViews: [
              { view: 'monitor_app.view.HostCurrentMetricsBox', rect: '0 0 800 735', anchors: 'left top right bottom'},
              { view: 'monitor_app.view.HostHistoryMetricsBox', rect: '0 0 800 735', anchors: 'left top right bottom', id: 'hostHistoryMetricsBox'}
            ] 
          }
        ]
      })[0];
      
		this.appendChild(mainBox);
		
    uki('#metricSummaryRefreshButton', mainBox).bind('click', function() {
      dataLoader.getHostSummaryMetrics(function(data) {
        uki('#hostSummaryMetricsTable', mainBox).data(eval(data));
      });
    });
    
    uki('#hostSummaryMetricsTable List', mainBox).bind('click', function() {
      var indexes = this.selectedIndexes();
      if(indexes.length != 1) {
        return;
      }
      var row = this.selectedRows()[0];
      if (!row) return;
      
      uki('#hostNameLabel').text(row.hostName);

      //clear history metric table
      var hostHistoryMetricsTableLength =uki('#hostHistoryMetricsTable').data().length;
      for(var i = 0; i < hostHistoryMetricsTableLength; i++) {
        uki('#hostHistoryMetricsTable').removeRow(0);
      }
            
      //fill current metric table
      dataLoader.getHostCurrentMetrics(row.hostName, '', function(data) {
        uki('#hostCurrentMetricsTable', mainBox).data(eval(data)).layout();
      });

      //set item group select
      dataLoader.getHostItemGroup(row.hostName, function(data) {
        var maxLength = 0;
        var options = uki.map(eval(data), function(eachRow) {
          if(eachRow['groupName'].length > maxLength) {
            maxLength = eachRow['groupName'].length;
          }
          return { value: eachRow['groupName'], text: eachRow['groupName'] }
        });
        var selectRect = uki('#hostHistoryMetricBoxGroupName', mainBox).rect();
        selectRect.width = maxLength * 8;
        uki('#hostHistoryMetricBoxGroupName', mainBox).options(options);
        uki('#hostHistoryMetricBoxGroupName', mainBox).rect(selectRect).layout();
        
        var buttonRect = uki('#hostHistoryQueryButton').rect();
        buttonRect.x = selectRect.x + selectRect.width + 10;
        uki('#hostHistoryQueryButton').rect(buttonRect).layout();
      });
      
      window.frames['hostChartFrame'].setHostName(row.hostName);
    });    
	};
});
