(function() {
// define namespace
monitor_app = {};

// all core modules
include('frameworks/uki/uki-core.js');

// used views, comment out unused ones
include('frameworks/uki/uki-view/view/box.js');
include('frameworks/uki/uki-view/view/image.js');
include('frameworks/uki/uki-view/view/button.js');
include('frameworks/uki/uki-view/view/checkbox.js');
include('frameworks/uki/uki-view/view/radio.js');
include('frameworks/uki/uki-view/view/textField.js');
include('frameworks/uki/uki-view/view/label.js');
include('frameworks/uki/uki-view/view/list.js');
include('frameworks/uki/uki-view/view/table.js');
include('frameworks/uki/uki-view/view/slider.js');
include('frameworks/uki/uki-view/view/splitPane.js');
include('frameworks/uki/uki-view/view/scrollPane.js');
include('frameworks/uki/uki-view/view/popup.js');
include('frameworks/uki/uki-view/view/flow.js');
include('frameworks/uki/uki-view/view/toolbar.js');

// theme
include('frameworks/uki/uki-theme/airport.js');

//data
include('frameworks/uki/uki-data/ajax.js');
include('frameworks/uki/uki-data/model.js');

include('frameworks/uki/uki-more.js');

include('monitor_app/view/metrics/hostCurrentMetricsBox.js');
include('monitor_app/view/metrics/hostHistoryMetricsBox.js');
include('monitor_app/view/metrics/metricsBox.js');
include('monitor_app/view/tabPanel.js');
include('monitor_app/view/nativeIframe.js');
include('monitor_app/view/menuBox.js');
include('monitor_app/view/item/itemBox.js');
include('monitor_app/view/service/serviceBox.js');
include('monitor_app/view/zookeeper/zkBox.js');
include('monitor_app/view/hosts/hostBox.js');
include('monitor_app/view/hadoop/hadoopMetricsBox.js');
include('monitor_app/view/hadoop/hdfsMetricsBox.js');
include('monitor_app/view/hadoop/mapredMetricsBox.js');
include('monitor_app/data/dataLoader.js');
include('monitor_app/data/zkDataLoader.js');
include('monitor_app/layout/main.js');
include('monitor_app/controller/main.js');


include('monitor_app/jquery-1.3.2.min.js');
include('monitor_app/jquery.format-1.0.min.js');

uki.theme.airport.imagePath = 'i/';

// skip interface creation if we're testing
if (window.TESTING) return;

monitor_app.controller.main();

})();

