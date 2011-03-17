include('../controller.js');

var dataLoader = new MonitorDataLoader();
var zkDataLoader = new ZKDataLoader();
var hostModel;

monitor_app.controller.main = function() {
  // create the views
  var serviceDataLoaded = false;
  var itemDataLoaded = false;
  var hostDataLoaded = false;
  
  var mainContext = monitor_app.layout.main();

  mainContext.attachTo(window, '1000 1000');

  var currentContentsBox = uki('#metricsBox', mainContext);

  var menus = new Array();
  menus[0] = uki('#menu_metrics', mainContext);
  menus[1] = uki('#menu_item', mainContext);
  menus[2] = uki('#menu_service', mainContext);
  menus[3] = uki('#menu_host', mainContext);    
  menus[4] = uki('#menu_zookeeper', mainContext);    
  menus[5] = uki('#menu_hadoop', mainContext);    
 
  dataLoader.getHostSummaryMetrics(function(data) {
    uki('#hostSummaryMetricsTable', mainContext).data(eval(data));
  });
  
  uki('#menu_metrics', mainContext).bind('click', function() {
    toggleMenu(0);
    currentContentsBox.visible(false);
    currentContentsBox = uki('#metricsBox', mainContext);
    currentContentsBox.visible(true).layout();
  });

  uki('#menu_item', mainContext).bind('click', function() {
    toggleMenu(1);
    currentContentsBox.visible(false);
    currentContentsBox = uki('#itemBox', mainContext);
    currentContentsBox.visible(true).layout();
    if(!itemDataLoaded) {
      dataLoader.getMonitorItems(function(data) {
        var evaledData = eval(data);
        uki('#itemsTable').data(evaledData);  
        if(evaledData.length > 0) {
          uki('#itemsTable List').selectedIndex(0);
        }
      });
      itemDataLoaded = true;
    }  
  });

  uki('#menu_service', mainContext).bind('click', function() {
    toggleMenu(2);
    currentContentsBox.visible(false);
    currentContentsBox = uki('#serviceBox', mainContext);
    currentContentsBox.visible(true).layout();
    if(!serviceDataLoaded) {
      dataLoader.getServiceGroupAsArray(function(data) {
        uki('#serviceList', mainContext).data(data);
        if(data) {
          uki('#serviceList', mainContext).selectedIndex(0);
        }
      });
      serviceDataLoaded = true;
    }
  });

  uki('#menu_host', mainContext).bind('click', function() {
    toggleMenu(3);
    currentContentsBox.visible(false);
    currentContentsBox = uki('#hostBox', mainContext);
    currentContentsBox.visible(true).layout();
    if(!hostDataLoaded) {
      dataLoader.getHosts(function(data) {
        hostModel = eval(data);
        uki('#hostTable', mainContext).data(hostModel);
        if(data) {
          uki('#hostTable', mainContext).selectedIndex(0);
        }
      });
      hostDataLoaded = true;      
    }
  });
  
  uki('#menu_zookeeper', mainContext).bind('click', function() {
    toggleMenu(4);
    currentContentsBox.visible(false);
    currentContentsBox = uki('#zkBox', mainContext);
    currentContentsBox.visible(true).layout();
  });
    
  uki('#menu_hadoop', mainContext).bind('click', function() {
    toggleMenu(5);
    currentContentsBox.visible(false);
    currentContentsBox = uki('#hadoopBox', mainContext);
    currentContentsBox.visible(true).layout();
  });
  
  toggleMenu = function(selectedMenu) {
    uki.each(menus, function(index, value) {
      if(index == selectedMenu) {
        value.background('cssBox(background:#E1E1E1;)');
      } else {
        value.background('');
      }    
    });
  }
};