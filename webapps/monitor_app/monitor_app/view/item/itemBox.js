include('../../view.js');

uki.view.declare('monitor_app.view.ItemBox', uki.view.Box, function(Base) {
  var systemMonitorAdaptorName = "SystemMonitorAdaptor";
  var itemAddView;
  var itemModifyView;
  var itemDeployView;
  var mainBox;
  
  var systemGroups = ["cpu", "memory", "swap", "disk", "network"];
  var systemItems = { "cpu": ["load", "user", "sys", "idle", "wait"], 
                      "memory": ["used", "free", "total"], 
                      "swap": ["swapUsed", "swapPageOut", "swapPageIn", "swapFree", "swapTotal"], 
                      "disk": ["size", "avail", "used", "usePercent"], 
                      "network": ["rxBytes", "txBytes"]
                    };
  
  var groupOptions = new Array();
  var index = 0;
  
  uki.each(systemItems, function(key, value) {
    groupOptions[index++] = { value: key, text: key };
  });
  
  var firstOptions = new Array();
  uki.each(systemItems[systemGroups[0]], function(i, value) {
    firstOptions[i] = { value: value, text: value };
  });

  var adaptorOption = new Array();

  uki.ajax({
    url: '/monitor?action=GetMonitorItemAdaptors',
    //url: '/adaptor.html',
    async: false,
    success: function(data) {
      if(data == '' || data == undefined) {
        data = [];
      }
      uki.each(eval(data), function(i, adaptor) {
        uki.each(adaptor, function(key, value1) {
          adaptorOption[i] = { value: key, text: key };
        });
      });      
    }
  });
    
  this._createDom = function() {
    Base._createDom.call(this);
      
    mainBox = uki.build({ view: 'VSplitPane', rect: '800 960', anchors: 'left top right bottom', handleWidth: 5, handlePosition: 395, minTop: 100,
                          topChildViews: [
                            { view: 'Box', rect: '0 0 800 30', anchors: 'left top right bottom', 
                              childViews: [ {view: 'Button', rect: '5 5 60 22', anchors: 'left top', text:'Query', id:'itemQueryButton'},
                                            {view: 'Button', rect: '70 5 60 22', anchors: 'left top', text:'Add', id:'itemAddViewButton'},
                                            {view: 'Button', rect: '135 5 60 22', anchors: 'left top', text:'Modify', id:'itemModifyViewButton'},
                                            {view: 'Button', rect: '200 5 60 22', anchors: 'left top', text:'Delete', id:'itemDeleteButton'},
                                            {view: 'Button', rect: '265 5 60 22', anchors: 'left top', text:'Deploy', id:'itemDeployViewButton'}
                                          ] 
                            },
                            { view: 'Table', rect: '0 30 800 365', anchors: 'left top right bottom', id:'itemsTable',
                              style: { fontSize: '12px', lineHeight: '12px' }, multiselect: false, 
                              columns: [
                                { view: 'table.Column', label: 'Item Id', key: 'itemId', width: 100, resizable: true},
                                { view: 'table.Column', label: 'Item Group', key:'groupName', width: 100, resizable: true},
                                { view: 'table.Column', label: 'Item Name', key:'itemName', width: 100, resizable: true},
                                { view: 'table.Column', label: 'Adaptor', key:'adaptorClass', width: 200, resizable: true},
                                { view: 'table.Column', label: 'Period', key: 'period', width: 100, resizable: true},
                                { view: 'table.Column', label: 'Default', key: 'defaultItem', width: 100, resizable: true},
                                //{ view: 'table.Column', label: 'Description', key:'description', width: 150, resizable: true},
                                { view: 'table.Column', label: 'alarm', key:'alarmExpr', width: 150, resizable: true},
                                { view: 'table.Column', label: 'mail', key:'alarmTo', width: 150, resizable: true}
                              ]
                            }
                          ],
                          bottomChildViews: [
                            { view: 'Box', rect: '0 0 800 70', anchors: 'left top', 
                              childViews: [
                                { view: 'Label', rect: '0 0 6000 30', anchors: 'left top', text: 'Selected Monitor Item', style: {'font-weight': 'bold'}, 
                                  inset: '8 10', background: 'cssBox(background:#D2D2D2;border:1px solid #999)' },
                                { view: 'Button', rect: '200 5 130 22', anchors: 'left top', text:'Modify Host Alarm', id:'itemModifyHostViewButton'},
                                { view: 'Box', rect: '0 30 6000 40', anchors: 'left top right bottom', background: 'cssBox(background:#E1E1E1;border-bottom:1px solid #999)',
                                  childViews: [
                                    { view: 'Label', rect: '10 10 100 20', anchors: 'left top', text: 'Item Name:', style: {'font-weight': 'bold'} },
                                    { view: 'Label', rect: '100 10 300 20', anchors: 'left top', id: 'itemDetailViewItemName'},
                                    { view: 'Label', rect: '310 10 100 20', anchors: 'left top', text: 'Parameter:', style: {'font-weight': 'bold'} },
                                    { view: 'Label', rect: '400 10 280 20', anchors: 'left top', id: 'itemDetailViewParams'}
                                  ]
                                }
                              ]
                            },
                            { view: 'Table', rect: '0 70 800 490', anchors: 'left top right bottom', id: 'itemHostsTable',
                              style: { fontSize: '12px', lineHeight: '12px' }, multiselect: true, 
                              columns: [
                                { view: 'table.Column', label: 'Host Name', key: 'hostName', width: 200, resizable: true},
                                { view: 'table.Column', label: 'Ip', key: 'hostIp', width: 100, resizable: true},
                                { view: 'table.Column', label: 'alarm expr', key: 'alarmExpr', width: 300, resizable: true},
                                { view: 'table.Column', label: 'alarm to', key: 'alarmTo', width: 300, resizable: true},
                                { view: 'table.Column', label: 'occur', key: 'occurTimes', width: 50, resizable: true}
                              ]
                            } 
                          ]
                        })[0]; 
    
    this.appendChild(mainBox);
    
    //Modal protect div 
    this.appendChild(
      uki.build(
        { view: 'Box', rect: '0 0 1000 1000', anchors: 'left top right bottom', visible:false, id: 'itemBoxPopupProtectBox'}
      )[0]);
    
    //init popup
    initAddItemView(this);
    initModifyItemView(this);
    initDeployItemView(this);
    initModifyHostItemView(this);

    uki('#itemsTable List', mainBox).bind('click', function() {
      var row = this.selectedRows()[0];
      if (!row) {
        return;
      }
      uki('#itemDetailViewItemId').text(row.itemId);
      uki('#itemDetailViewGroupName').text(row.groupName);
      uki('#itemDetailViewItemName').text(row.itemName);
      uki('#itemDetailViewAdaptor').text(row.adaptorClass);
      uki('#itemDetailViewPeriod').text(row.period + " sec");
      uki('#itemDetailViewDefault').text(row.defaultItem);
      uki('#itemDetailViewParams').text(row.params);
      uki('#itemDetailViewDescription').text(row.description);
      
      //get host by itemid
      dataLoader.getMonitorItemHosts(row.itemId, function(data) {
        uki('#itemHostsTable').data(eval(data));
      });         
    });
    
    uki('#itemQueryButton', mainBox).bind('click', function() {
      dataLoader.getMonitorItems(function(data) {
        uki('#itemsTable').data(eval(data));  
      });
    });
        
    uki('#itemAddViewButton', mainBox).bind('click', function() {
      showItemPopup();
      uki('#itemAddViewBox').visible(true).layout();
      uki('#itemAddViewAdaptor').value(systemMonitorAdaptorName);
      uki('#itemAddViewItemGroupSelect').value(systemGroups[0]);
    });
    
    uki('#itemModifyViewButton', mainBox).bind('click', function() {
      var row = uki('#itemsTable').selectedRows()[0];
      if(!row) { 
        alert('Select item');
        return;
      }
      dataLoader.getMonitorItem(row.itemId, function(originData) {
        if(originData == '' || originData == undefined) {
          alert('No item data');
          return;
        }
        var data = eval(originData)[0];
        showItemPopup();
        uki('#itemModifyViewBox').visible(true).layout();

        if(data['adaptorClass'] == systemMonitorAdaptorName) {
          uki('#itemModifyViewParam').attributes({readonly:true}).layout();
        } else {
          uki('#itemModifyViewParam').attributes({readonly:false}).layout();
        }
        uki('#itemModifyViewItemId').value(data['itemId']);
        uki('#itemModifyViewItemName').text("Modify Item: " + data['groupName'] + "." + data['itemName']); 
        uki('#itemModifyViewPeriod').value(data['period']);
        uki('#itemModifyViewParam').value(data['params']); 
        uki('#itemModifyViewDescription').value(data['description']);
        uki('#itemModifyViewAlarmExpr').value(data['alarmExpr']);
        uki('#itemModifyViewAlarmOccurTimes').value(data['occurTimes']);
        uki('#itemModifyViewAlarmTo').value(data['alarmTo']);
      }); 
    });    
        
    uki('#itemModifyHostViewButton').bind('click', function() {
      var row = uki('#itemHostsTable').selectedRows()[0];
      if(!row) { 
        alert('Select host');
        return;
      }
      var itemId = uki('#itemsTable').selectedRows()[0].itemId;
      
      showItemPopup();
      uki('#hostItemModifyViewBox').visible(true).layout();

      uki('#hostItemModifyViewItemId').value(itemId);
      uki('#hostItemModifyViewHostName').value(row.hostName);
      uki('#hostItemModifyViewAlarmExpr').value(row.alarmExpr);
      uki('#hostItemModifyViewAlarmOccurTimes').value(row.occurTimes);
      uki('#hostItemModifyViewAlarmTo').value(row.alarmTo);
    });

    uki('#itemDeleteButton', mainBox).bind('click', function() {
      var row = uki('#itemsTable').selectedRows()[0];
      if(!row) { 
        alert('Select item');
        return;
      }
      
      if(!confirm("Delete Item [" + row.itemName + "]")) {
        return;
      }
      
      dataLoader.deleteMonitorItem(row.itemId, function(data) {
        if(data == 'success') {
          alert('Deleted');
          uki('#itemQueryButton', mainBox).trigger('click');
        }
      })
    });
    
    uki('#itemDeployViewButton', mainBox).bind('click', function() {
      //set label
      var itemId = uki('#itemsTable').selectedRow().itemId;
      uki('#itemDeployViewTitle').text("Deploy Item(" + uki('#itemsTable').selectedRow().groupName + "." + uki('#itemsTable').selectedRow().itemName + ")");
      uki('#itemDeployViewItemId').text(itemId);
      
      //get host by itemid
      dataLoader.getMonitorItemHosts(itemId, function(data) {
        var hostNames = new Array();
        var hostInfos = eval(data);
        for(var i = 0; i < hostInfos.length; i++) {
          hostNames[i] = hostInfos[i]['hostName'];
        }
        uki('#itemDeployViewAddedHosts').data(hostNames);
      });    
      
      //get all hosts            
      dataLoader.getServiceHosts('', function(data) {
        var hostNames = new Array();
        uki.each(eval(data), function(index, value) {
          hostNames[index] = value['hostName'];
        });
        uki('#itemDeployViewHosts').data(hostNames);
      });
      
      //visible
      showItemPopup();
      uki('#itemDeployViewBox').visible(true).layout();
      
      //get services
      dataLoader.getServiceGroupAsArray(function(data) {
        var services = data;
        var serviceOptions = new Array();
        serviceOptions[0] = {value:'all', text:'all'};
        if(services != '' && services.length != undefined) {
          uki.each(services, function(index, value) {
            serviceOptions[index+1] = {value:value, text:value};
          });
        }
        uki('#itemDeployViewService').options(serviceOptions);  
        //uki('#itemDeployViewService').value('all');
        //uki('#itemDeployViewService').trigger('change');
      });        
    });
  };
  
  initDeployItemView = function(_this) {
    var popRect = _this.rect().clone();
    var popWidth = 500;
    var popHeight = 400;
    popRect.x = popRect.x + (popRect.width/2 - popWidth/2);
    popRect.y = 100;
    popRect.width = popWidth;
    popRect.height = popHeight;
  
    itemDeployView = uki.build( { view: 'Box', rect: popRect, anchors: 'left top', id: 'itemDeployViewBox', background: 'cssBox(background:#E1E1E1;border:1px solid #999)', visible: false,
                                  childViews: [
                                    { view: 'Label', rect: '10 0 300 30', anchors: 'left top', text: 'Deploy Item', style: {'font-weight': 'bold'}, id: 'itemDeployViewTitle'},
                                    { view: 'Label', rect: '10 0 100 30', anchors: 'left top', text: '', visible:false, id: 'itemDeployViewItemId'},
                                    { view: 'Box', rect: '0 30 500 330', anchors: 'left top',
                                        childViews: [   
                                          { view: 'Box', rect: '0 0 210 330', anchors: 'left top',
                                            childViews: [
                                              { view: 'Select', rect: '10 0 190 22', anchors: 'left top', rowHeight: 22, id:'itemDeployViewService'},
                                              { view: 'Box', rect: '10 30 190 300', anchors: 'top left right', background: '#CCC',
                                                childViews: [
                                                  {view: 'uki.view.List', rect: '1 1 188 298', anchors: 'left top', rowHeight: 22, id: 'itemDeployViewHosts', throttle: 0, multiselect: true, textSelectable: false}
                                                ]
                                              }
                                            ]
                                          },
                                          { view: 'Box', rect: '210 0 80 330', anchors: 'left top',
                                            childViews: [
                                              { view: 'Button', rect: '10 150 60 20', anchors: 'left top', text: 'add', id: 'itemDeployViewAddButton'},
                                              { view: 'Button', rect: '10 175 60 20', anchors: 'left top', text: 'del', id: 'itemDeployViewDelButton'}
                                            ]
                                          },
                                          { view: 'Box', rect: '290 0 210 330', anchors: 'left top',
                                            childViews: [
                                              { view: 'Box', rect: '10 30 190 300', anchors: 'top left right', background: '#CCC',
                                                childViews: [
                                                  {view: 'uki.view.List', rect: '1 1 188 298', anchors: 'left top', rowHeight: 22, id:'itemDeployViewAddedHosts', throttle: 0, multiselect: true, textSelectable: false}
                                                ]
                                              }
                                            ]
                                          }
                                        ]
                                    },
                                    { view: 'Button', rect: '190 370 60 22', anchors: 'left top', text: 'Save', id: 'itemDeployViewSaveButton'},
                                    { view: 'Button', rect: '255 370 60 22', anchors: 'left top', text: 'Cancel', id: 'itemDeployViewCancelButton'}
                                  ]
                                })[0];

    _this.appendChild(itemDeployView);    
    
    uki('#itemDeployViewService', itemDeployView).bind('change', function() {
      //get service hosts
      var selectedService = uki('#itemDeployViewService').value();
      if(selectedService == 'all') {
        selectedService = '';
      }
      dataLoader.getServiceHosts(selectedService, function(data) {
        var listValues = new Array();
        uki.each(eval(data), function(index, value) {
          listValues[index] = value['hostName'];
        });
        uki('#itemDeployViewHosts').data(listValues);
      });
    });
        
    uki('#itemDeployViewAddButton', itemDeployView).bind('click', function() {
      var leftList = uki('#itemDeployViewHosts');
      var rightList = uki('#itemDeployViewAddedHosts');

      var rows = leftList.selectedRows();
      var indexes = leftList.selectedIndexes();
      uki.each(rows, function(i, value) {
        if(uki.inArray(leftList.data()[indexes[i]], rightList.data()) >= 0) {
        } else {
          rightList.addRow(rightList.data().length, value);
        }
      });
    });

    uki('#itemDeployViewDelButton', itemDeployView).bind('click', function() {
      var rightList = uki('#itemDeployViewAddedHosts');

      var indexes = rightList.selectedIndexes();
      uki.each(indexes, function(i, value) {
        rightList.removeRow(value);
      });
    });  
 
    uki('#itemDeployViewSaveButton', itemDeployView).bind('click', function() {
      var itemId = uki('#itemDeployViewItemId').text();
      var hosts = new Array();
      uki.each(uki('#itemDeployViewAddedHosts').data(), function(i, value) {
        hosts[i] = value;
      });
      dataLoader.addItemToHost(itemId, hosts, function(result) {
        if(result == 'success') {
          alert('Success');
          //uki('#itemsTable').selectedIndex(uki('#itemsTable').selectedIndex());
          uki('#itemsTable List').trigger('click');
        }
      });
      
      hideDeployView();
    });
 
    uki('#itemDeployViewCancelButton', itemDeployView).bind('click', function() {
      hideDeployView();
    });
  };

  initAddItemView = function(_this) {
    var popRect = _this.rect().clone();
    var popWidth = 500;
    var popHeight = 340;
    popRect.x = popRect.x + (popRect.width/2 - popWidth/2);
    popRect.y = 100;
    popRect.width = popWidth;
    popRect.height = popHeight;

    itemAddView = uki.build({ view: 'Box', rect: popRect, anchors: 'left top', id: 'itemAddViewBox', 
                              background: 'cssBox(background:#E1E1E1;border:1px solid #999)', visible: false, style: {'zIndex': '9001'},
                              childViews: [
                                { view: 'Label', rect: '10 0 100 30', anchors: 'left top', text: 'Add Item', style: {'font-weight': 'bold'}},
                                { view: 'VFlow', rect: '10 30 500 200', anchors: 'left top', id:'addItemLabelVFlow',
                                  childViews: [
                                    { view: 'Box', rect: '0 0 400 25', anchors: 'right top',
                                      childViews: [
                                        {view: 'Label', rect: '0 0 70 25', anchors: 'right top', inset: '1 5', text:'Adaptor'},
                                        {view: 'Select', rect: '70 0 400 22', anchors: 'left top', rowHeight: 22, id:'itemAddViewAdaptor',
                                          options: adaptorOption
                                        }      
                                      ]
                                    },
                                    { view: 'Box', rect: '0 0 400 25', anchors: 'right top',
                                      childViews: [
                                         {view: 'TextField', rect: '0 0 10 10', anchors: 'right top', visible:false, id: 'itemAddViewItemId'},
                                         {view: 'Label', rect: '0 0 70 25', anchors: 'right top', inset: '1 5', text:'Item Group'},
                                         {view: 'TextField', rect: '70 0 160 22', anchors: 'left top', id: 'itemAddViewItemGroup', visible:false},
                                         {view: 'Select', rect: '70 0 160 22', anchors: 'left top', id: 'itemAddViewItemGroupSelect', options:groupOptions, value: systemGroups[0]},
                                         {view: 'Label', rect: '240 0 70 25', anchors: 'right top', inset: '1 5', text:'Item Name'},
                                         {view: 'TextField', rect: '310 0 160 22', anchors: 'left top', id: 'itemAddViewItemName', visible:false},
                                         {view: 'Select', rect: '310 0 160 22', anchors: 'left top', id: 'itemAddViewItemNameSelect', options:firstOptions, value: systemItems[systemGroups[0]][0]}  
                                      ]
                                    },
                                    { view: 'Box', rect: '0 0 400 25', anchors: 'right top',
                                      childViews: [
                                         {view: 'Label', rect: '0 0 70 25', anchors: 'right top', inset: '1 5', text:'Period'},
                                         {view: 'TextField', rect: '70 0 50 22', anchors: 'left top', id: 'itemAddViewPeriod' },
                                         {view: 'Label', rect: '122 0 50 20', anchors: 'right top', inset: '1 5', text:'Sec'},
                                         {view: 'Label', rect: '240 0 70 25', anchors: 'right top', inset: '1 5', text:'Default'},
                                         {view: 'Checkbox', rect: '310 0 24 22', anchors: 'left top', id: 'itemAddViewDefault' }
                                      ]
                                    },
                                    { view: 'Box', rect: '0 0 400 55', anchors: 'right top',
                                      childViews: [
                                         {view: 'Label', rect: '0 0 70 25', anchors: 'right top', inset: '1 5', text:'Command'},
                                         {view: 'MultilineTextField', rect: '10 25 460 30', anchors: 'left top', id: 'itemAddViewParam', attributes: {readonly:true}}
                                      ]
                                    },
                                    { view: 'Box', rect: '0 0 400 55', anchors: 'right top',
                                      childViews: [
                                         {view: 'Label', rect: '0 0 70 25', anchors: 'right top', inset: '1 5', text:'Description'},
                                         {view: 'MultilineTextField', rect: '10 25 460 30', anchors: 'left top', id: 'itemAddViewDescription' }
                                      ]
                                    },
                                    { view: 'Box', rect: '0 0 400 75', anchors: 'right top',
                                      childViews: [
                                         {view: 'Label', rect: '0 0 70 25', anchors: 'right top', inset: '1 5', text:'Alarm Condition'},
                                         {view: 'MultilineTextField', rect: '10 25 460 30', anchors: 'left top', id: 'itemAddViewAlarmExpr' },
                                         {view: 'Label', rect: '0 60 70 25', anchors: 'right top', inset: '1 5', text:'Occur Times'},
                                         {view: 'TextField', rect: '100 60 130 22', anchors: 'left top', id: 'itemAddViewAlarmOccurTimes' },
                                         {view: 'Label', rect: '240 60 30 25', anchors: 'right top', inset: '1 5', text:'To'},
                                         {view: 'TextField', rect: '270 60 200 22', anchors: 'left top', id: 'itemAddViewAlarmTo' }
                                      ]
                                    }                                    
                                  ]
                                },
                                { view: 'Button', rect: '190 310 60 22', anchors: 'left top', text: 'Save', id: 'itemAddViewSaveButton', style: {'zIndex': '8999'}},
                                { view: 'Button', rect: '255 310 60 22', anchors: 'left top', text: 'Cancel', id: 'itemAddViewCancelButton'}
                              ]})[0];
  
    _this.appendChild(itemAddView);

    uki('#itemAddViewSaveButton', itemAddView).bind('click', function() {
      if(uki('#itemAddViewItemName').value() == "") {
        alert('no item name');
        return;
      } 
      if(uki('#itemAddViewItemGroup').value() == "") {
        alert('no item group');
        return;
      }
      if(uki('#itemAddViewPeriod').value() == "") {
        alert('no period');
        return;
      }
      if(uki('#itemAddViewAdaptor').value() != systemMonitorAdaptorName && uki('#itemAddViewParam').value() == "") {
        alert('no param'); 
        return;
      }    
      dataLoader.addMonitorItem(
          uki('#itemAddViewItemId').value(),
          uki('#itemAddViewItemName').value(), 
          uki('#itemAddViewItemGroup').value(), 
          uki('#itemAddViewAdaptor').value(), 
          uki('#itemAddViewDefault').checked(), 
          uki('#itemAddViewPeriod').value(), 
          uki('#itemAddViewParam').value(), 
          uki('#itemAddViewDescription').value(), 
          uki('#itemAddViewAlarmExpr').value(), 
          uki('#itemAddViewAlarmOccurTimes').value(),
          uki('#itemAddViewAlarmTo').value(), 
          function(result) {
            if(result == 'success') {
              alert('Success adding monitor item: ' + uki('#itemAddViewItemName').value());      
              dataLoader.getServiceGroup(function() {
                dataLoader.getMonitorItems(function(data) {
                  uki('#itemsTable').data(eval(data));  
                });  
              });
              hideAddView();
            } else {
              alert('fail:' + result);
            }
      });      
    });
    
    
    uki('#itemAddViewCancelButton', itemAddView).bind('click', function() {
      hideAddView();
    });
    
    uki('#itemAddViewAdaptor').change(function() {
      if(this.value() == systemMonitorAdaptorName) {
        uki('#itemAddViewItemGroupSelect').visible(true);
        uki('#itemAddViewItemNameSelect').visible(true);

        uki('#itemAddViewItemGroup').visible(false).layout();
        uki('#itemAddViewItemName').visible(false).layout();
        uki.attr(uki('#itemAddViewParam'), 'attributes', {readonly:true}).layout();
        
        uki('#itemAddViewItemGroupSelect').value(systemGroups[0]);
      } else {
        uki('#itemAddViewItemGroupSelect').visible(false);
        uki('#itemAddViewItemNameSelect').visible(false);

        uki('#itemAddViewItemGroup').value('');
        uki('#itemAddViewItemName').value('');
        uki('#itemAddViewItemGroup').visible(true).layout();
        uki('#itemAddViewItemName').visible(true).layout();
        uki.attr(uki('#itemAddViewParam'), 'attributes', {readonly:false}).layout();
      }
    });
    
    uki('#itemAddViewItemGroupSelect').change(function() {
      var itemOptions = new Array();
      var selectedValue;
      uki.each(systemItems[this.value()], function(i, value) {
        itemOptions[i] = { value: value, text: value };
        if(i == 0) {
          selectedValue = value;
        }
      });
      uki('#itemAddViewItemNameSelect').options(itemOptions);
      uki('#itemAddViewItemNameSelect').value(selectedValue);
      uki('#itemAddViewItemGroup').value(this.value());
      uki('#itemAddViewItemName').value(selectedValue);
    });
    
    uki('#itemAddViewItemNameSelect').change(function() {
      uki('#itemAddViewItemName').value(this.value());
    });
    
    uki('#itemAddViewItemGroup').value(uki('#itemAddViewItemGroupSelect').value());
    uki('#itemAddViewItemName').value(uki('#itemAddViewItemNameSelect').value());    
  };
  
  initModifyItemView = function(_this) {
    var popRect = _this.rect().clone();
    var popWidth = 500;
    var popHeight = 320;
    popRect.x = popRect.x + (popRect.width/2 - popWidth/2);
    popRect.y = 100;
    popRect.width = popWidth;
    popRect.height = popHeight;

    itemModifyView = uki.build(
                            { view: 'Box', rect: popRect, anchors: 'left top', id: 'itemModifyViewBox', background: 'cssBox(background:#E1E1E1;border:1px solid #999)', visible: false,
                              childViews: [
                                { view: 'Label', rect: '10 0 500 30', anchors: 'left top', text: 'Modify Item', style: {'font-weight': 'bold'}, id: 'itemModifyViewItemName'},
                                { view: 'Box', rect: '10 30 500 240', anchors: 'left top',
                                  childViews: [
                                    { view: 'Box', rect: '0 0 500 25', anchors: 'right top',
                                      childViews: [
                                         {view: 'TextField', rect: '0 0 10 10', anchors: 'right top', visible:false, id: 'itemModifyViewItemId'},
                                         {view: 'Label', rect: '0 0 70 25', anchors: 'right top', inset: '1 5', text:'Period'},
                                         {view: 'TextField', rect: '70 0 50 22', anchors: 'left top', id: 'itemModifyViewPeriod' },
                                         {view: 'Label', rect: '122 0 50 20', anchors: 'right top', inset: '1 5', text:'Sec'},
                                         {view: 'Label', rect: '175 0 150 25', anchors: 'right top', inset: '1 5', text:'Deploy alarm to hosts'},
                                         {view: 'Checkbox', rect: '330 0 24 22', anchors: 'left top', id: 'itemModifyViewDeployAlarmHost' }
                                      ]
                                    },
                                    { view: 'Box', rect: '0 30 500 55', anchors: 'right top',
                                      childViews: [
                                         {view: 'Label', rect: '0 0 70 25', anchors: 'right top', inset: '1 5', text:'Command'},
                                         {view: 'MultilineTextField', rect: '10 25 460 30', anchors: 'left top', id: 'itemModifyViewParam'}
                                      ]
                                    },
                                    { view: 'Box', rect: '0 90 500 55', anchors: 'right top',
                                      childViews: [
                                         {view: 'Label', rect: '0 0 70 25', anchors: 'right top', inset: '1 5', text:'Description'},
                                         {view: 'MultilineTextField', rect: '10 25 460 30', anchors: 'left top', id: 'itemModifyViewDescription' }
                                      ]
                                    },
                                    { view: 'Box', rect: '0 150 500 75', anchors: 'right top',
                                      childViews: [
                                         {view: 'Label', rect: '0 0 70 25', anchors: 'right top', inset: '1 5', text:'Alarm Condition'},
                                         {view: 'MultilineTextField', rect: '10 25 460 30', anchors: 'left top', id: 'itemModifyViewAlarmExpr' },
                                         {view: 'Label', rect: '0 60 70 25', anchors: 'right top', inset: '1 5', text:'Occur Times'},
                                         {view: 'TextField', rect: '100 60 130 22', anchors: 'left top', id: 'itemModifyViewAlarmOccurTimes' },
                                         {view: 'Label', rect: '240 60 30 25', anchors: 'right top', inset: '1 5', text:'To'},
                                         {view: 'TextField', rect: '270 60 200 22', anchors: 'left top', id: 'itemModifyViewAlarmTo' }
                                      ]
                                    }                                    
                                  ]
                                },
                                { view: 'Button', rect: '190 280 60 22', anchors: 'left top', text: 'Save', id: 'itemModifyViewSaveButton'},
                                { view: 'Button', rect: '255 280 60 22', anchors: 'left top', text: 'Cancel', id: 'itemModifyViewCancelButton'}
                              ]})[0];
  
    _this.appendChild(itemModifyView);

    uki('#itemModifyViewSaveButton', itemModifyView).bind('click', function() {
      if(uki('#itemModifyViewPeriod').value() == "") {
        alert('no period');
        return;
      }
      var autoDeploy = "N";
      if(uki('#itemModifyViewDeployAlarmHost').checked()) {
        autoDeploy = "Y";
      }
      dataLoader.modifyMonitorItem(
          uki('#itemModifyViewItemId').value(),
          uki('#itemModifyViewPeriod').value(), 
          uki('#itemModifyViewParam').value(), 
          uki('#itemModifyViewDescription').value(), 
          uki('#itemModifyViewAlarmExpr').value(), 
          uki('#itemModifyViewAlarmOccurTimes').value(),
          uki('#itemModifyViewAlarmTo').value(), 
          autoDeploy,
          function(result) {
            if(result == 'success') {
              alert('Success modify(' + uki('#itemAddViewItemName').value() + ' item)');      
              dataLoader.getServiceGroup(function() {
                dataLoader.getMonitorItems(function(data) {
                  uki('#itemsTable').data(eval(data));  
                });  
              });
              hideModifyView();
            } else {
              alert('fail:' + result);
            }
      });      
    });
    
    uki('#itemModifyViewCancelButton', itemModifyView).bind('click', function() {
      hideModifyView();
    });
  };

  initModifyHostItemView = function(_this) {
    var popRect = _this.rect().clone();
    var popWidth = 500;
    var popHeight = 170;
    popRect.x = popRect.x + (popRect.width/2 - popWidth/2);
    popRect.y = 100;
    popRect.width = popWidth;
    popRect.height = popHeight;

    hostItemModifyView = uki.build(
                            { view: 'Box', rect: popRect, anchors: 'left top', id: 'hostItemModifyViewBox', background: 'cssBox(background:#E1E1E1;border:1px solid #999)', visible: false,
                              childViews: [
                                { view: 'Label', rect: '10 0 500 30', anchors: 'left top', text: 'Modify Item', style: {'font-weight': 'bold'}},
                                { view: 'Box', rect: '10 30 500 85', anchors: 'left top',
                                  childViews: [
                                    {view: 'TextField', rect: '0 0 10 10', anchors: 'right top', visible:false, id: 'hostItemModifyViewItemId'},
                                    {view: 'TextField', rect: '0 0 10 10', anchors: 'right top', visible:false, id: 'hostItemModifyViewHostName'},
                                    {view: 'Label', rect: '0 0 70 25', anchors: 'right top', inset: '1 5', text:'Alarm Condition'},
                                    {view: 'MultilineTextField', rect: '10 25 460 30', anchors: 'left top', id: 'hostItemModifyViewAlarmExpr' },
                                    {view: 'Label', rect: '0 60 70 25', anchors: 'right top', inset: '1 5', text:'Occur Times'},
                                    {view: 'TextField', rect: '100 60 130 22', anchors: 'left top', id: 'hostItemModifyViewAlarmOccurTimes' },
                                    {view: 'Label', rect: '240 60 30 25', anchors: 'right top', inset: '1 5', text:'To'},
                                    {view: 'TextField', rect: '270 60 200 22', anchors: 'left top', id: 'hostItemModifyViewAlarmTo' }
                                  ]
                                },
                                { view: 'Button', rect: '190 130 60 22', anchors: 'left top', text: 'Save', id: 'hostItemModifyViewSaveButton'},
                                { view: 'Button', rect: '255 130 60 22', anchors: 'left top', text: 'Cancel', id: 'hostItemModifyViewCancelButton'}
                              ]
                            })[0];
  
    _this.appendChild(hostItemModifyView);
    uki('#hostItemModifyViewSaveButton', hostItemModifyView).bind('click', function() {
      dataLoader.modifyHostMonitorItem(
        uki('#hostItemModifyViewItemId').value(),
        uki('#hostItemModifyViewHostName').value(), 
        uki('#hostItemModifyViewAlarmExpr').value(), 
        uki('#hostItemModifyViewAlarmOccurTimes').value(),
        uki('#hostItemModifyViewAlarmTo').value(), 
        function(result) {
          if(result == 'success') {
            alert('Success');      
            dataLoader.getMonitorItemHosts(uki('#hostItemModifyViewItemId').value(), function(data) {
              uki('#itemHostsTable').data(eval(data));
            });  
            hideHostItemModifyView();
          } else {
            alert('fail:' + result);
          }
        });      
    });

    uki('#hostItemModifyViewCancelButton', hostItemModifyView).bind('click', function() {
      hideHostItemModifyView();
    });
  };
    
  hideAddView = function() {
    hideItemPopup();
    uki('#itemAddViewItemId').value(''); 
    uki('#itemAddViewItemName').value(''); 
    uki('#itemAddViewItemGroup').value(''); 
    uki('#itemAddViewAdaptor').value('');
    uki('#itemAddViewDefault').checked(false); 
    uki('#itemAddViewPeriod').value('');
    uki('#itemAddViewParam').value(''); 
    uki('#itemAddViewDescription').value(''); 
    uki('#itemAddViewAlarmExpr').value('');
    uki('#itemAddViewAlarmOccurTimes').value('');
    uki('#itemAddViewAlarmTo').value('');    
    
    uki('#itemAddViewBox').visible(false).layout();
  };
  
  hideModifyView = function() {
    hideItemPopup();
    uki('#itemModifyViewItemId').value(''); 
    uki('#itemModifyViewPeriod').value('');
    uki('#itemModifyViewParam').value(''); 
    uki('#itemModifyViewDescription').value(''); 
    uki('#itemModifyViewAlarmExpr').value('');
    uki('#itemModifyViewAlarmOccurTimes').value('');
    uki('#itemModifyViewAlarmTo').value('');    
    
    uki('#itemModifyViewBox').visible(false).layout();
  };  

  hideHostItemModifyView = function() {
    hideItemPopup();
    uki('#hostItemModifyViewItemId').value(''); 
    uki('#hostItemModifyViewHostName').value('');
    uki('#hostItemModifyViewAlarmExpr').value('');
    uki('#hostItemModifyViewAlarmOccurTimes').value('');
    uki('#hostItemModifyViewAlarmTo').value('');    
    
    uki('#hostItemModifyViewBox').visible(false).layout();
  }; 
    
  hideDeployView = function() {
    hideItemPopup();
    uki('#itemDeployViewBox').visible(false).layout();
  };
  
  showItemPopup = function() {
    uki('#itemBoxPopupProtectBox').visible(true).layout();
  };
  
  hideItemPopup = function() {
    uki('#itemBoxPopupProtectBox').visible(false).layout();
  };
});


