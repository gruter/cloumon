include('../../view.js');

monitor_app.view.menuBox = {};

uki.view.declare('monitor_app.view.HdfsMetricsBox', uki.view.Box, function(Base) {
  var mainBox;
  
  this._createDom = function() {
    Base._createDom.call(this);
		mainBox = uki.build({ view: 'VSplitPane', rect: '0 0 800 930', anchors: 'left top right bottom', handleWidth: 5, handlePosition: 230, minTop: 100,
                          topChildViews: [ 
                            { view: 'Box', rect: '0 0 800 230', anchors: 'left top right bottom', background: 'cssBox(background:#FFFFFF;border:1px solid #999)',
                              childViews: [
                                { view: 'Box', rect: '0 0 800 30', anchors: 'left top right bottom', 
                                  childViews: [ 
                                    {view: 'Button', rect: '730 5 60 22', anchors: 'right top', text:'Query', id:'nameNodeRefreshButton'}
                                  ] 
                                },
                                { view: 'Box', rect: '0 30 800 200', anchors: 'left top right bottom',
                                  childViews: [
                                    { view: 'uki.view.NativeIframe', rect: '0 0 800 200', anchors: 'left top right bottom', id:'nameNodeChartFrame', name:'nameNodeChartFrame', src:'/namenode_chart.html'}
                                  ]
                                }                            
                              ]
                            }
                          ],
                          bottomChildViews: [ 
                            { view: 'Box', rect: '0 0 800 700', anchors: 'left top right bottom',
                              childViews: [
                                { view: 'Box', rect: '0 0 250 700', anchors: 'left top right bottom', background: 'cssBox(background:#FFFFFF;border:1px solid #999)',
                                  childViews: [
                                    { view: 'Table', rect: '0 0 250 700', anchors: 'left top right bottom', id: 'dataNodeHostsTable', multiselect: false, style: {fontSize: '12px', lineHeight: '12px'},
                                      columns: [
                                        { view: 'table.Column', label: 'Host', key: 'hostName', width: 150, resizable: true},
                                        { view: 'table.Column', label: 'Type', key: 'nodeType', width: 50, resizable: true},
                                        { view: 'table.Column', label: 'Status', key: 'live', width: 50, resizable: true}
                                      ]
                                    }                                    
                                  ]
                                },
                                { view: 'Box', rect: '250 0 5 700', anchors: 'left top right bottom', background: 'cssBox(background:#FFFFFF;border:1px solid #999)' },
                                { view: 'Box', rect: '255 0 300 700', anchors: 'left top right bottom', background: 'cssBox(background:#FFFFFF;border:1px solid #999)',
                                  childViews: [
                                    { view: 'Table', rect: '0 0 260 700', anchors: 'left top right bottom', id: 'dataNodeMetricsTable', multiselect: false, style: {fontSize: '12px', lineHeight: '12px'},
                                      columns: [
                                        { view: 'table.Column', label: 'Item Name', key: 'itemName', width: 220, resizable: true},
                                        { view: 'table.Column', label: 'Data', key: 'monitorData', width: 60, resizable: true}
                                      ]
                                    }
                                  ]
                                },
                                { view: 'Box', rect: '555 0 5 700', anchors: 'left top right bottom', background: 'cssBox(background:#FFFFFF;border:1px solid #999)' },
                                { view: 'Box', rect: '560 0 240 700', anchors: 'left top right bottom', background: 'cssBox(background:#FFFFFF;border:1px solid #999)',
                                  childViews: [
                                    { view: 'uki.view.NativeIframe', rect: '0 0 240 700', anchors: 'left top right bottom', id:'dataNodeChartFrame', name:'dataNodeChartFrame', src:'/datanode_chart.html'}
                                  ]
                                }                                                                 
                              ]
                            }
                          ]
                        })[0]; 
      
		this.appendChild(mainBox);
		
	  uki('#dataNodeHostsTable List', mainBox).bind('click', function() {
      var indexes = this.selectedIndexes();
      if(indexes.length != 1) {
        return;
      }
      var row = this.selectedRows()[0];
      if (!row) return;
      
      //clear history metric table
      var tableLength =uki('#dataNodeMetricsTable').data().length;
      for(var i = 0; i < tableLength; i++) {
        uki('#dataNodeMetricsTable').removeRow(0);
      }
            
      //fill current metric table
      dataLoader.getHostCurrentMetrics(row.hostName, row.nodeType, function(data) {
        uki('#dataNodeMetricsTable', mainBox).data(eval(data)).layout();
      });
    }); 
    
    uki('#dataNodeMetricsTable List', mainBox).bind('click', function() {
      var selectedHostName = uki('#dataNodeHostsTable').selectedRows()[0].hostName;
      var selectedType = uki('#dataNodeHostsTable').selectedRows()[0].nodeType;
      var indexes = this.selectedIndexes();
      if(indexes.length != 1) {
        return;
      }
      var row = this.selectedRows()[0];
      if (!row) return;
      
      //chart
      dataLoader.getHostHistoryMetrics(selectedHostName, selectedType, row.itemName, '', '', function(data) {
          var jsonData = eval(data); 
          window.frames['dataNodeChartFrame'].showGraph(jsonData);
      });
    });     
    
		uki('#nameNodeRefreshButton', mainBox).bind('click', function() {
      loadDataNode();
      loadNameNode();
    });
    
    loadNameNode = function() {
      dataLoader.getHostHistoryMetrics('namenode', 'namenode', 'CapacityRemainingGB,CapacityTotalGB,CapacityUsedGB', '', '', function(data) {
          var jsonData = eval(data); 
          window.frames['nameNodeChartFrame'].showGraph(1, jsonData);
        }
      );    
      dataLoader.getHostHistoryMetrics('namenode', 'namenode', 'FilesTotal,BlocksTotal', '', '', function(data) {
          var jsonData = eval(data); 
          window.frames['nameNodeChartFrame'].showGraph(2, jsonData);
        }
      ); 
      dataLoader.getHostHistoryMetrics('namenode', 'namenode', 'Transactions_avg_time,Transactions_num_ops', '', '', function(data) {
          var jsonData = eval(data); 
          window.frames['nameNodeChartFrame'].showGraph(3, jsonData);
        }
      );      
      dataLoader.getHostHistoryMetrics('namenode', 'namenode', 'memHeapCommittedM,memHeapUsedM,memHeapCommittedM,memNonHeapUsedM', '', '', function(data) {
          var jsonData = eval(data); 
          window.frames['nameNodeChartFrame'].showGraph(4, jsonData);
        }
      );         
    };
    
    loadDataNode = function() {
      dataLoader.getDataNodeHosts( function(data) {
        var jsonData = eval(data); 
        uki('#dataNodeHostsTable').data(jsonData);
      });     
    };
	};
});
