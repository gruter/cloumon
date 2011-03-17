include('../../view.js');

monitor_app.view.menuBox = {};

uki.view.declare('monitor_app.view.MapredMetricsBox', uki.view.Box, function(Base) {
  var mainBox;
  
  this._createDom = function() {
    Base._createDom.call(this);
		mainBox = uki.build(
        { view: 'Box', rect: '0 0 800 935', anchors: 'left top right bottom'
           
        })[0];
      
		this.appendChild(mainBox);
	};
});
