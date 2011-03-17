include('../layout.js');

monitor_app.layout.main = function() {
	return uki(
    { view: 'Box', rect: '0 0 1000 1000', anchors: 'left top right bottom',
		  childViews: [
		    //Top View
		    { view: 'Box', rect: '0 0 1000 40', anchors: 'left top right', id: 'topBox', 
		      background: 'cssBox(background:#FFFFFF;border-bottom:1px solid #999)',
          childViews: [
            { view: 'Image', rect: '10 8 100 27', anchors: 'left top', src: '/images/cloumon.gif'},
            { view: 'Label', rect: '850 20 150 20', anchors: 'right bottom', text:'© 2010 www.cloumon.org'}
          ]
		    },
		    //MainView
		    { view: 'HSplitPane', rect: '0 40 1000 960', anchors: 'left top right bottom', handlePosition: 195, handleWidth: 5,
		      //background: 'cssBox(background:#FFFFFF;border-top:1px solid #999)',
          //Menu
          leftChildViews: [
            { view: 'monitor_app.view.MenuBox', rect: '0 0 195 960', anchors: 'left top right bottom', id: 'menuBox' }
          ],
          //Contents
          rightChildViews: [
            { view: 'Box', rect: '0 0 800 960', anchors: 'left top right bottom', id: 'contentsBox',
              childViews: [
                { view: 'monitor_app.view.MetricsBox', rect: '0 0 800 960', anchors: 'left top right bottom', id: 'metricsBox', visible: true},
                { view: 'monitor_app.view.ItemBox', rect: '0 0 800 960', anchors: 'left top right bottom', id: 'itemBox', visible: false},
            	  { view: 'monitor_app.view.ServiceBox', rect: '0 0 800 960', anchors: 'left top right bottom', id: 'serviceBox', visible: false},
                { view: 'monitor_app.view.HostBox', rect: '0 0 800 960', anchors: 'left top right bottom', id: 'hostBox', visible: false},
                { view: 'monitor_app.view.ZkBox', rect: '0 0 800 960', anchors: 'left top right bottom', id: 'zkBox', visible: false},
                { view: 'monitor_app.view.HadoopMetricsBox', rect: '0 0 800 960', anchors: 'left top right bottom', id: 'hadoopBox', visible: false}
              ]
            }
          ]
  		  }
	    ]}
	);

/*
  return uki(
    { view: 'Box', rect: '0 0 1000 1000', anchors: 'left top',
      childViews: [
        { view: 'Button', rect: '0 0 400 50', text:'test', anchor:'left top right bottom', id:'testButton'},  
        { view: 'monitor_app.view.TabPanel', rect: '0 50 1000 950', anchors: 'left top right bottom',
          tabLables: [ { text: 'Current'}, { text: 'History'} ],
          tabViews: [
            { view: 'Table', rect: '0 0 1000 975', anchors: 'left top right bottom', id:'testTable', style: {fontSize: '12px', lineHeight: '12px'},
              columns: [
              ]
            },
            { view: 'Label', rect: '0 0 1000 975', anchors: 'left top right bottom', text:'History'}
          ] 
        }
      ]
    }
  );
*/
/*
  return uki( 
        { view: 'Box', rect: '0 0 1000 1000', anchors: 'left top right bottom',
        childViews: [
        { view: 'ScrollPane', rect: '0 0 1000 1000', anchors: 'left top right bottom',
          childViews: [
            { view: 'List', rect: '1000 1000', anchors: 'left top right bottom',
              id:'testList', style: {fontSize: '12px', lineHeight: '12px'},
            }
          ]
        }
        ]}
      );

*/

/*
  return uki( 
        { view: 'Box', rect: '0 0 1000 1000', anchors: 'left top',
          childViews: [
            { view: 'Button', rect: '0 0 400 50', text:'test', anchor:'left top right bottom', id:'testButton'},
            { view: 'Table', rect: '0 100 1000 500', anchors: 'left top right bottom',
              id:'testTable', style: {fontSize: '12px', lineHeight: '12px'},
              columns: [
              ]
            }
          ]
        }
      );
*/

/*
  return uki( 
        { view: 'HFlow', rect: '1000 1000', anchors: 'left top', 
          childViews: [
            { view: 'Button', rect: '100 25', anchors: 'left top', text:'History'},
            { view: 'Table', rect: '0 25 1000 975', anchors: 'left top right bottom',
              id:'testTable', style: {fontSize: '12px', lineHeight: '12px'},
              columns: [
                    { view: 'table.Column', label: 'time', key:'col1', resizable: true, width: 100},
                    { view: 'table.Column', label: 'aaa', key:'col2', resizable: true, width: 200 },
                    { view: 'table.NumberColumn', label: 'bbb', key:'col3', resizable: true, width: 100}
              ]
            }
          ]
        }
      );
        



  return uki( 
        { view: 'VSplitPane', rect: '1000 1000', anchors: 'left top right bottom', handleWidth: 5, handlePosition: 195,
          topChildViews: [
            { view: 'Label', rect: '0 0 1000 600', anchors: 'left top right', text:'History'}
          ],
          bottomChildViews: [
            { view: 'Table', rect: '0 0 1000 800', minSize: '0 200', anchors: 'left top right bottom',
              id:'testTable', style: {fontSize: '12px', lineHeight: '12px'},
              columns: [
                    { view: 'table.Column', label: 'time', key:'col1', resizable: true, width: 100},
                    { view: 'table.Column', label: 'aaa', key:'col2', resizable: true, width: 200 },
                    { view: 'table.NumberColumn', label: 'bbb', key:'col3', resizable: true, width: 100}
              ]
            }
          ]
        }
      );
 */     
};