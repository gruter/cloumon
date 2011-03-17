include('../../view.js');

uki.view.declare('monitor_app.view.ServiceBox', uki.view.Box, function(Base) {
  var mainBox;
  var serviceAddView;
  var serviceAddHostView;
  
  this._createDom = function() {
    Base._createDom.call(this);
      
    mainBox = uki.build({ view: 'Box', rect: '0 0 800 960', anchors: 'left top right bottom', 
                          childViews: 
                            [ { view: 'Box', rect: '0 0 800 30', anchors: 'left top right bottom', //background: 'cssBox(background:#FFFFFF;border-bottom:1px solid #999)',
                                childViews: [     
                                       {view: 'Button', rect: '5 5 60 22', anchors: 'left top', text:'Refresh', id:'serviceRefreshButton'},
                                       {view: 'Button', rect: '70 5 60 22', anchors: 'left top', text:'Add', id:'serviceAddViewButton'},
                                       {view: 'Button', rect: '140 5 60 22', anchors: 'left top', text:'Delete', id:'serviceDeleteButton'}, 
                                       {view: 'Button', rect: '240 5 100 22', anchors: 'left top', text:'Add Host', id:'serviceAddHostViewButton'},
                                       {view: 'Button', rect: '345 5 100 22', anchors: 'left top', text:'Delete Host', id:'serviceDeleteHostButton'}]
                              },
                              { view: 'HSplitPane', rect: '0 30 800 930', anchors: 'left top right bottom', handleWidth: 3, handlePosition: 300, leftMin: 300, 
                                leftChildViews: [ 
                                  { view: 'ScrollPane', rect: '0 0 300 930', anchors: 'top left right bottom',             
                                    childViews: 
                                      { view: 'List', rect: '0 0 300 930', anchors: 'top left right bottom', rowHeight: 22, id: 'serviceList', throttle: 0, multiselect: false, textSelectable: true }
                                  } ], //leftChildViews
                                rightChildViews: [
                                  { view: 'Box', rect: '0 0 500 930', anchors: 'top left right', background: '#CCC',
                                    childViews:   
                                      { view: 'Table', rect: '0 0 500 930', anchors: 'left top right bottom', id: 'serviceHostsTable',
                                        style: { fontSize: '12px', lineHeight: '12px' }, multiselect: true, 
                                        columns: [
                                          { view: 'table.Column', label: 'Host Name', key: 'hostName', width: 300, resizable: true},
                                          { view: 'table.Column', label: 'Ip', key: 'hostIp', width: 200, resizable: true}
                                        ]
                                      } 
                                  }]  //rightChildViews
                              }
                            ]  //childViews
                        })[0]; 
    
    this.appendChild(mainBox);
    
    //Modal protect div 
    this.appendChild(
      uki.build(
        { view: 'Box', rect: '0 0 1000 1000', anchors: 'left top right bottom', visible:false, id: 'serviceBoxPopupProtectBox'}
      )[0]);
          
    initAddServiceView(this);
    initAddServiceHostView(this);
    
     uki('#serviceList').bind('click', function() {
      dataLoader.getServiceHosts(this.selectedRow(), function(data) {
        uki('#serviceHostsTable').data(eval(data));
      });
    });
  
    uki('#serviceRefreshButton').bind('click', function() {
      dataLoader.getServiceGroupAsArray(function(data) {
        uki('#serviceList').data(data);
        if(data) {
          uki('#serviceList').selectedIndex(0);
        }
      });
    });
    
    uki('#serviceAddViewButton', mainBox).bind('click', function() {
      showPopup();
      uki('#serviceAddViewBox').visible(true).layout();
    });
    
    uki('#serviceDeleteButton', mainBox).bind('click', function() {
      var row = uki('#serviceList').selectedRows()[0];
      if(!row) { 
        alert('Select Service');
        return;
      }
      
      if(!confirm("Delete Service group [" + row + "]")) {
        return;
      }
      
      dataLoader.deleteServiceGroup(row, function(data) {
        if(data == 'success') {
          alert('Deleted');
          uki('#serviceRefreshButton').trigger('click');
        }
      })
    });
    
    uki('#serviceAddHostViewButton', mainBox).bind('click', function() {
      showPopup();
      uki('#serviceAddHostViewBox').visible(true).layout();
      uki('#serviceAddHostViewServiceName').text('Add Hosts(' + uki('#serviceList').selectedRow() +')');
      
      dataLoader.getHosts(function(data) {
        var hostNames = new Array();
        uki.each(eval(data), function(index, value) {
          hostNames[index] = value['hostName'];
        });
        uki('#serviceAddHostViewHosts').data(hostNames);
      });
    });  
    
    uki('#serviceDeleteHostButton', mainBox).bind('click', function() {
      var serviceName = uki('#serviceList').selectedRows()[0];
      if(!serviceName) { 
        alert('Select Service');
        return;
      }
      
      var rows = uki('#serviceHostsTable').selectedRows();
      if(rows.length == 0) {
        alert('Select Host');
        return;
      }
      
      if(!confirm("Delete Service hosts [" + rows.length + "]")) {
        return;
      }
      
      var hostNames = "";
      for(var i = 0; i < rows.length; i++) {
        hostNames = hostNames + rows[i].hostName + ",";
      }
      dataLoader.deleteServiceGroupHosts(serviceName, hostNames, function(data) {
        if(data == 'success') {
          alert('Deleted');
          dataLoader.getServiceHosts(uki('#serviceList').selectedRow(), function(data) {
            uki('#serviceHostsTable').data(eval(data));
          });
        } else {
          alert(data);
        }
      })
    });    
      
  };
  
  initAddServiceView = function(_this) {
    var popRect = _this.rect().clone();
    var popWidth = 300;
    var popHeight = 100;
    popRect.x = popRect.x + (popRect.width/2 - popWidth/2);
    popRect.y = 100;
    popRect.width = popWidth;
    popRect.height = popHeight;

    serviceAddView = uki.build(
          { view: 'Box', rect: popRect, anchors: 'left top', id: 'serviceAddViewBox', background: 'cssBox(background:#E1E1E1;border:1px solid #999)', visible: false,
            childViews: [
              { view: 'Label', rect: '10 0 300 30', anchors: 'left top', text: 'Add Service', style: {'font-weight': 'bold'}},
              { view: 'Box', rect: '0 30 300 25', anchors: 'right top',
                childViews: [
                  { view: 'Label', rect: '10 0 90 25', anchors: 'right top', inset: '1 5', text:'Service Name'},
                  { view: 'TextField', rect: '100 0 180 22', anchors: 'left top', id: 'serviceAddViewServiceName'},
                  { view: 'Button', rect: '80 30 60 22', anchors: 'left top', text: 'Save', id: 'serviceAddViewSaveButton'},
                  { view: 'Button', rect: '145 30 60 22', anchors: 'left top', text: 'Cancel', id: 'serviceAddViewCancelButton'}
                ]
              }
            ]
          })[0];
  
    _this.appendChild(serviceAddView);      
    
    uki('#serviceAddViewSaveButton', serviceAddView).bind('click', function() {
      dataLoader.addServiceGroup(uki('#serviceAddViewServiceName', serviceAddView).value(), function(result) {
        if(result == 'success') {
          alert('Success');      
          dataLoader.getServiceGroupAsArray(function(data) {
            uki('#serviceList').data(data);
            if(data) {
              uki('#serviceList').selectedIndex(0);
            }
          });
          hideAddServiceView();
        } else {
          alert('fail:' + result);
        }
      });
    });
    
    uki('#serviceAddViewCancelButton', serviceAddView).bind('click', function() {
      hideAddServiceView();
    });
  };
  
  initAddServiceHostView = function(_this) {
    var popRect = _this.rect().clone();
    var popWidth = 500;
    var popHeight = 400;
    popRect.x = popRect.x + (popRect.width/2 - popWidth/2);
    popRect.y = 100;
    popRect.width = popWidth;
    popRect.height = popHeight;

    serviceAddHostView = uki.build({ view: 'Box', rect: popRect, anchors: 'left top', id: 'serviceAddHostViewBox', background: 'cssBox(background:#E1E1E1;border:1px solid #999)',
                visible: false,
                  childViews: [{ view: 'Label', rect: '10 0 350 30', anchors: 'left top', text: 'Add Hosts: ', id:'serviceAddHostViewServiceName', style: {'font-weight': 'bold'}},
                               { view: 'Box', rect: '0 30 500 330', anchors: 'left top',
                          childViews: [   { view: 'Box', rect: '0 0 210 330', anchors: 'left top',
                                    childViews: [
                                          {view: 'Label', rect: '10 0 200 30', anchors: 'top left right bottom', text: 'Hosts'},
                                    {view: 'ScrollPane', rect: '10 30 195 300', anchors: 'top left right bottom',             
                                      childViews: { view: 'Box', rect: '0 0 195 300', anchors: 'top left right', background: '#CCC',
                                            childViews: { view: 'List', rect: '1 1 194 298', anchors: 'top left right', 
                                                rowHeight: 22, id: 'serviceAddHostViewHosts', throttle: 0, multiselect: true, textSelectable: false }
                                      }
                                    }
                                     ]},
                                    { view: 'Box', rect: '210 0 80 330', anchors: 'left top',
                                      childViews: [
                                          { view: 'Button', rect: '10 150 60 20', anchors: 'left top', text: 'add', id: 'serviceAddHostViewAddButton'},
                                          { view: 'Button', rect: '10 175 60 20', anchors: 'left top', text: 'del', id: 'serviceAddHostViewDelButton'}
                                    ]},
                                    { view: 'Box', rect: '290 0 210 330', anchors: 'left top',
                                      childViews: [
                                            {view: 'Label', rect: '10 0 200 30', anchors: 'top left right bottom', text: 'Added Hosts'},
                                      {view: 'ScrollPane', rect: '10 30 195 300', anchors: 'top left right bottom',             
                                        childViews: { view: 'Box', rect: '0 0 195 300', anchors: 'top left right', background: '#CCC',
                                              childViews: { view: 'List', rect: '1 1 194 298', anchors: 'top left right', 
                                                  rowHeight: 22, id: 'serviceAddHostViewAddedHosts', throttle: 0, multiselect: true, textSelectable: false }
                                        }
                                      }
                                       ]}
                                ]
                               },
                         { view: 'Button', rect: '150 370 80 22', anchors: 'left top', text: 'Save', id: 'serviceAddHostViewSaveButton'},
                         { view: 'Button', rect: '235 370 80 22', anchors: 'left top', text: 'Cancel', id: 'serviceAddHostViewCancelButton'}
                               ]
    })[0];
  
    _this.appendChild(serviceAddHostView);    
    
    uki('#serviceAddHostViewSaveButton', serviceAddHostView).bind('click', function() {
      var hosts = new Array();
      uki.each(uki('#serviceAddHostViewAddedHosts').data(), function(i, value) {
        hosts[i] = value;
      });
      dataLoader.addHostsToService(uki('#serviceList').selectedRow(), hosts, function(result) {
        if(result == 'success') {
          alert('Success');
          dataLoader.getServiceHosts(uki('#serviceList').selectedRow(), function(data) {
            uki('#serviceHostsTable').data(eval(data));
          });
        }      
      });
       
      hideAddHostView();
    });
    
    uki('#serviceAddHostViewCancelButton', serviceAddHostView).bind('click', function() {
      hideAddHostView();
    });
    
    uki('#serviceAddHostViewAddButton', serviceAddHostView).bind('click', function() {
      var leftList = uki('#serviceAddHostViewHosts');
      var rightList = uki('#serviceAddHostViewAddedHosts');

      var rows = leftList.selectedRows();
      var indexes = leftList.selectedIndexes();
      uki.each(rows, function(i, value) {
        if(uki.inArray(leftList.data()[indexes[i]], rightList.data()) >= 0) {
        } else {
          rightList.addRow(rightList.data().length, value);
        }
      });
    });
    
    uki('#serviceAddHostViewDelButton', serviceAddHostView).bind('click', function() {
      var rightList = uki('#serviceAddHostViewAddedHosts');

      var indexes = rightList.selectedIndexes();
      uki.each(indexes, function(i, value) {
        rightList.removeRow(value);
      });
    });   
  };
  
  hideAddServiceView = function() {
    hidePopup();
    uki('#serviceAddViewServiceName').value('');
    uki('#serviceAddViewBox').visible(false).layout();
  };
  
  hideAddHostView = function() {
    hidePopup();
    uki('#serviceAddHostViewAddedHosts').data([]);
    uki('#serviceAddHostViewBox').visible(false).layout();
  };

  showPopup = function() {
    uki('#serviceBoxPopupProtectBox').visible(true).layout();
  };
  
  hidePopup = function() {
    uki('#serviceBoxPopupProtectBox').visible(false).layout();
  };  
});


