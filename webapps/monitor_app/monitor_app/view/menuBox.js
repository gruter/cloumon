include('../view.js');

//monitor_app.view.menuBox = {};

uki.view.declare('monitor_app.view.MenuBox', uki.view.Box, function(Base) {
  this._createDom = function() {
    Base._createDom.call(this);
		var menu = uki.build(
		  { view: 'ScrollPane', rect: '195 960', anchors: 'left top right bottom', 
			  childViews: [
           { view: 'Label', rect: '0 10 9999 25', anchors: 'left top', id: 'menu_metrics', text: 'Metrics', inset: '0 0 0 10', background: 'cssBox(background:#E1E1E1;)'},
           { view: 'Label', rect: '0 40 9999 25', anchors: 'left top', id: 'menu_item', text: 'Item', inset: '0 0 0 10'},
           { view: 'Label', rect: '0 70 9999 25', anchors: 'left top', id: 'menu_service', text: 'Service',  inset: '0 0 0 10'},
           { view: 'Label', rect: '0 100 9999 25', anchors: 'left top', id: 'menu_host', text: 'Host',  inset: '0 0 0 10'},
           { view: 'Label', rect: '0 130 9999 25', anchors: 'left top', id: 'menu_zookeeper', text: 'ZooKeeper',  inset: '0 0 0 10'},
           { view: 'Label', rect: '0 160 9999 25', anchors: 'left top', id: 'menu_hadoop', text: 'Hadoop',  inset: '0 0 0 10'}           
         ]
      })[0];
    this.appendChild(menu);
	};	
});