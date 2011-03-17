include('../../view.js');

monitor_app.view.hostBox = {};

uki.view.declare('monitor_app.view.HostBox', uki.view.Box, function(Base) {
  var mainBox;
  var modifyAlarmView;
  
  this._createDom = function() {
    Base._createDom.call(this);
    mainBox = uki.build(
      { view: 'VSplitPane', rect: '0 0 800 960', anchors: 'left top right bottom', handleWidth: 5, handlePosition: 195,
        topChildViews: [ 
          { view: 'Box', rect: '0 0 800 30', anchors: 'left top right bottom', 
            childViews: [
              { view: 'Button', rect: '530 5 60 22', anchors: 'right top', background: 'theme(field)', text: 'Delete', id: 'deleteHostButton' },
              { view: 'Button', rect: '600 5 100 22', anchors: 'right top', background: 'theme(field)', text: 'Modify Alarm', id: 'hostAlarmButton' },
              { view: 'Button', rect: '710 5 80 22', anchors: 'right top', background: 'theme(field)', text: 'Refresh', id: 'hostRefreshButton' }
            ] 
          },
          { view: 'Box', rect: '0 30 800 170', anchors: 'left top right bottom', 
            childViews: [
              //server list table
              { view: 'Table', rect: '0 0 800 170', anchors: 'left top right bottom', id:'hostTable',
                style: { fontSize: '12px', lineHeight: '12px' }, multiselect: false,
                columns: [
                  { view: 'table.Column', label: 'Host Name', width: 150, resizable: true, key: 'hostName' },
                  { view: 'table.Column', label: 'Status', width: 50, resizable: true, key: 'liveStatus' },
                  { view: 'table.Column', label: 'Host Ip', width: 100, resizable: true, key: 'hostIps' }, 
                  { view: 'table.NumberColumn', label: 'Cpu', width: 60, resizable: true, key: 'cpuinfo_totalCores' },
                  { view: 'table.NumberColumn', label: 'Memory', width: 60, resizable: true, key: 'meminfo_memory' },
                  { view: 'table.NumberColumn', label: 'Swap', width: 120, resizable: true, key: 'meminfo_swapTotal' },
                  { view: 'table.Column', label: 'File system', width: 200, resizable: true, key: 'fileSystemInfo' },
                  { view: 'table.Column', label: 'Network', width: 200, resizable: true, key: 'networkInfo' },
                  { view: 'table.Column', label: 'Alarm', width: 100, resizable: true, key: 'hostAlarm' },
                  { view: 'table.Column', label: 'On', width: 50, resizable: true, key: 'alarmOn' }
                ]
              }
            ]
          }
        ],
        bottomChildViews: [ 
          { view: 'ScrollPane', rect: '0 0 800 760', anchors: 'top left right bottom', scrollableH: true, scrollableV: true, 
            childViews: [
              { view: 'VFlow', rect: '0 0 800 760', anchors: 'top left right bottom', id: 'hostDetailInfo',
                childViews: [
                ] 
              }
            ]
          }
        ]
      })[0];
      
    this.appendChild(mainBox);
    
        //Modal protect div 
    this.appendChild(
      uki.build(
        { view: 'Box', rect: '0 0 1000 1000', anchors: 'left top right bottom', visible:false, id: 'hostBoxPopupProtectBox'}
      )[0]);
    
    initModifyAlarmView(this);
    
    uki('#hostAlarmButton').bind('click', function() {
      var rows = uki('#hostTable').selectedRows();
      if(rows.length == 0) {
        alert('Select Host');
        return;
      }
      uki('#hostBoxPopupProtectBox').visible(true).layout();
      uki('#modifyAlarmViewBox').visible(true).layout();
      uki('#modifyAlarmViewBoxHostAlarm').value(rows[0].hostAlarm);
      uki('#modifyAlarmViewBoxAlarmOn').checked(eval(rows[0].alarmOn));
    });
    
    uki('#deleteHostButton').bind('click', function() {
      var rows = uki('#hostTable').selectedRows();
      if(rows.length == 0) {
        alert('Select Host');
        return;
      }
      
      if(!confirm("Delete hosts [" + rows.length + "]")) {
        return;
      }
      
      var hostNames = "";
      for(var i = 0; i < rows.length; i++) {
        hostNames = hostNames + rows[i].hostName + ",";
      }
      dataLoader.deleteHosts(hostNames, function(data) {
        if(data == 'success') {
          alert('Deleted');
          uki('#hostRefreshButton').trigger('click');
        } else {
          alert(data);
        }
      })
    });
    
    uki('#hostRefreshButton').bind('click', function() {
      //remove previous data
      var childLength = uki('#hostDetailInfo').childViews().length;
      for(var i = 0; i < childLength; i++) {
        uki('#hostDetailInfo').removeChild(uki('#hostDetailInfo').childViews()[0]);        
      }
          
      dataLoader.getHosts(function(data) {
        hostModel = eval(data);
        uki('#hostTable').data(hostModel);
        if(data) {
          uki('#hostTable').selectedIndex(0);
        }
      });
    });
    
    uki('#hostTable List', mainBox).bind('click', function() {
      var indexes = this.selectedIndexes();
      if(indexes.length != 1) {
        return;
      }
      var row = this.selectedRows()[0];
      if (!row) return;
      
      //remove previous data
      var childLength = uki('#hostDetailInfo').childViews().length;
      for(var i = 0; i < childLength; i++) {
        uki('#hostDetailInfo').removeChild(uki('#hostDetailInfo').childViews()[0]);        
      }
      
      for ( name in hostModel[indexes[0]] ) {
        var text = name + ": " + hostModel[indexes[0]][name];
        var label = uki({ view: 'Label', rect: '0 0 300 22', anchors: 'left top', inset: '1 5', text:text, textSelectable: true});
        uki('#hostDetailInfo').append(label);
      } 
      //uki('#hostDetailInfo');        
    });    
  };
  
  initModifyAlarmView = function(_this) {
    var popRect = _this.rect().clone();
    var popWidth = 400;
    var popHeight = 140;
    popRect.x = popRect.x + (popRect.width/2 - popWidth/2);
    popRect.y = 100;
    popRect.width = popWidth;
    popRect.height = popHeight;

    modifyAlarmView = uki.build(
          { view: 'Box', rect: popRect, anchors: 'left top', id: 'modifyAlarmViewBox', background: 'cssBox(background:#E1E1E1;border:1px solid #999)', visible: false,
            childViews: [
              { view: 'Label', rect: '10 0 300 30', anchors: 'left top', text: 'Host Alarm', style: {'font-weight': 'bold'}},
              { view: 'Box', rect: '0 30 300 25', anchors: 'right top',
                childViews: [
                  { view: 'Label', rect: '10 0 90 25', anchors: 'right top', inset: '1 5', text:'Alarm To:'},
                  { view: 'TextField', rect: '100 0 280 22', anchors: 'left top', id: 'modifyAlarmViewBoxHostAlarm'},
                  { view: 'Label', rect: '10 40 90 25', anchors: 'right top', inset: '1 5', text:'Alarm On:'},
                  { view: 'Checkbox', rect: '100 40 20 22', anchors: 'left top', id: 'modifyAlarmViewBoxAlarmOn' },
                  { view: 'Button', rect: '160 70 60 22', anchors: 'left top', text: 'Save', id: 'modifyAlarmViewSaveButton'},
                  { view: 'Button', rect: '225 70 60 22', anchors: 'left top', text: 'Cancel', id: 'modifyAlarmViewCancelButton'}
                ]
              }
            ]
          })[0];
  
    _this.appendChild(modifyAlarmView);      
    
    uki('#modifyAlarmViewSaveButton', modifyAlarmView).bind('click', function() {
      var rows = uki('#hostTable').selectedRows();
      var hostAlarm = uki('#modifyAlarmViewBoxHostAlarm', modifyAlarmView).value();
      var alarmOn = uki('#modifyAlarmViewBoxAlarmOn', modifyAlarmView).checked();
      dataLoader.modifyHostAlarm(rows[0].hostName, hostAlarm, alarmOn, function(result) {
        if(result == 'success') {
          alert('Success');    
          uki('#hostRefreshButton').trigger('click');  
        } else {
          alert('fail:' + result);
        }
        uki('#modifyAlarmViewBox').visible(false).layout();
        uki('#hostBoxPopupProtectBox').visible(false).layout();
      });
    });
    
    uki('#modifyAlarmViewCancelButton', modifyAlarmView).bind('click', function() {
      uki('#modifyAlarmViewBox').visible(false).layout();
      uki('#hostBoxPopupProtectBox').visible(false).layout();
    });
  };
  
});
