include('../../view.js');

uki.view.declare('monitor_app.view.ZkBox', uki.view.Box, function(Base) {
  this._createDom = function() {
    Base._createDom.call(this);
      
    mainBox = uki.build(
      { view: 'Box', rect: '0 0 800 960', anchors: 'left top right bottom', 
        childViews: [
          { view: 'Box', rect: '800 30', anchors: 'left top right', background: '#E1E1E1',
            childViews: [
              { view: 'Label', rect: '10 3 90 22', anchors: 'left top', text:' zk servers: '},
              { view: 'TextField', rect: '90 3 300 22', anchors: 'left top', id: 'zkservers', value:'127.0.0.1:2181'},
              { view: 'Button', rect: '410 3 80 22', anchors: 'left top', id: 'zkRefreshButton', text:'Query'}
            ]
          },
          { view: 'ScrollPane', rect: '0 30 800 930', anchors: 'left top right bottom',
            childViews: [
              //{ view: 'Label', rect: '800 930', anchors: 'left top right bottom', html:'<div id="zkTree" class="zkTree">zkTreeDiv</div>'}
              {view: 'uki.view.NativeIframe', rect: '800 930', anchors: 'left top right bottom', id:'zkTreeFrame', name:'zkTreeFrame', src:'/zk_tree.html'}
            ]
          }
        ] 
      })[0]; 
    
    this.appendChild(mainBox);
    
    uki('#zkRefreshButton').bind('click', function() {
      window.frames['zkTreeFrame'].makeTree(uki('#zkservers').value(), "/");
    });
  };
});


