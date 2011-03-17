include('../../view.js');

monitor_app.view.hostCurrentMetricsBox = {};

uki.view.declare('monitor_app.view.HostCurrentMetricsBox', uki.view.Box, function(Base) {

  this._createDom = function() {
    Base._createDom.call(this);
    var rect = this.rect().clone();
    
    var toolRect = this.rect().clone();
    toolRect.height = 30;
    
    var toolBoxRect1 = this.rect().clone();
    toolBoxRect1.height = 30;
    toolBoxRect1.width = rect.width/2;
    
    var toolBoxRect2 = this.rect().clone();
    toolBoxRect2.height = 30;
    toolBoxRect2.width = rect.width/2;
    toolBoxRect2.x = rect.width/2;
    
    var tableRect = this.rect().clone();
    tableRect.y = 30 + 250;
    tableRect.height = 705 - 250;
    
    var toolFlow = uki.build(
        { view: 'Box', rect: '0 0 800 735', anchors: 'left top right bottom',
          childViews: [
            { view: 'Box', rect: '0 0 800 30', anchors: 'left top right bottom',
              childViews: [
                { view: 'Box', rect: '0 0 400 30', anchors: 'left top right bottom',
                  childViews: [ 
                    {view: 'Label', rect: '10 5 90 25', anchors: 'left top right bottom', text: 'Selected Host:'},
                    {view: 'Label', rect: '100 5 300 25', anchors: 'left top right bottom', id: 'hostNameLabel', style: {'font-weight': 'bold'} }
                  ]
                },
                { view: 'Box', rect: '400 0 400 30', anchors: 'left top right bottom',
                  childViews: [
                    {view: 'Button', rect: '330 5 60 22', anchors: 'top right', text: 'Refresh', id:'refreshButton'}
                  ]
                } 
              ]
            },
            { view: 'Box', rect: '0 30 1000 250', anchors: 'left top',
              childViews: [
                { view: 'uki.view.NativeIframe', rect: '0 0 1000 250', anchors: 'left top right bottom', id:'hostChartFrame', name:'hostChartFrame', src:'/host_chart.html'}
              ]
            },
            { view: 'Table', id: 'hostCurrentMetricsTable', rect: tableRect, anchors: 'left top right bottom', 
              columns: [
                { view: 'table.Column', label: 'group', key:'groupName', resizable: true, width: 100},
                { view: 'table.Column', label: 'resource', key:'resourceName', resizable: true, width: 200 },
                { view: 'table.Column', label: 'item', key:'itemName', resizable: true, width: 150},
                { view: 'table.NumberColumn', label: 'data', key:'monitorData', resizable: true, width: 150},
                { view: 'table.Column', label: 'log time', key:'timestamp', resizable: true, width: 200}
              ], 
              multiselect: true, style: {fontSize: '12px', lineHeight: '12px'} 
            }
          ]
	      })[0];
            
    this.appendChild(toolFlow);

    uki('#refreshButton', toolFlow).bind('click', function() {
      var indexes = uki('#hostSummaryMetricsTable').selectedIndexes();
      if(indexes.length != 1) {
        return;
      }
      var row = uki('#hostSummaryMetricsTable').selectedRows()[0];
      if (!row) return;
      
      dataLoader.getHostCurrentMetrics(row.hostName, '', function(data) {
        uki('#hostCurrentMetricsTable').data(eval(data));
      });
      
      uki('#hostHistoryMetricsTable').data([]);
      
      dataLoader.getHostItemGroup(row.hostName, function(data) {
        var options = uki.map(eval(data), function(eachRow) {
          return { value: eachRow['groupName'], text: eachRow['groupName'] }
        });
        uki('#hostHistoryMetricBoxGroupName').options(options);
      });
    });
	}
});


