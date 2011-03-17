include('../../view.js');

monitor_app.view.menuBox = {};

uki.view.declare('monitor_app.view.HadoopMetricsBox', uki.view.Box, function(Base) {
  var mainBox;
  
  this._createDom = function() {
    Base._createDom.call(this);
		mainBox = uki.build(
		    { view: 'Box', rect: '0 0 800 960', anchors: 'left top right bottom',
		      childViews: [
            { view: 'Box', rect: '0 0 800 30', anchors: 'left top right bottom',	
              childViews: [
                { view: 'Button', rect: '2 2 80 27', anchors: 'left top', background: 'theme(field)', text: 'hdfs', id: 'hdfsTabButton' },
                { view: 'Button', rect: '84 2 80 27', anchors: 'left top', background: 'theme(field)', text: 'mapred', id: 'mapredTabButton' }
              ]
            },	      
            { view: 'monitor_app.view.HdfsMetricsBox', rect: '0 30 800 930', anchors: 'left top right bottom', visible:true}
            //{ view: 'monitor_app.view.MapredMetricsBox', rect: '0 30 800 930', anchors: 'left top right bottom', visible:false}
          ]
        })[0];
      
		this.appendChild(mainBox);
	};
});
