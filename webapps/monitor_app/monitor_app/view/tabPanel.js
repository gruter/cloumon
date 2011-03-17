include("../view.js");

monitor_app.view.tabPanel = {};

uki.view.declare('monitor_app.view.TabPanel', uki.view.Container, function(Base) {

    this.typeName = function() { return 'monitor_app.view.TabPanel'; };

    var tabButtons;
    var tabViews;
    
    this._setup = function() {
        Base._setup.call(this);
        this._widths = [];
    };
    
    this.tabLables = uki.newProp('_tabLables', function(b) {
      tabButtons = uki.build(uki.map(b, this._createTabButton, this)).resizeToContents('width');
      this._tabLableBox.childViews(tabButtons);
      
      for(var i = 0; i < tabButtons.length; i++) {
        tabButtons[i].checked(false);
      }
      
      for(var i = 0; i < tabButtons.length; i++) {
        if(i == 0) {
          tabButtons[i].checked(true);
        } 
        tabButtons[i].bind('click', function() {
          for(var j = 0; j < tabButtons.length; j++) {
            if(tabButtons[j].text() == this.text()) {
              tabButtons[j].checked(true);
            } else {
              tabButtons[j].checked(false);
            }
            if(tabButtons[j].checked()) {
              tabViews[j].visible(true).layout();
            } else {
              tabViews[j].visible(false).layout();
            }                
          }
        }); 
      }
      this.setTabViewVisible();
    });
    
    this.tabViews = uki.newProp('_tabViews', function(b) {
      tabViews = uki.build(b);
      this._tabViewBox.childViews(tabViews);
      this.setTabViewVisible();
    });
    
    this.setTabViewVisible = function() {
      if(tabViews == undefined || tabViews.length == undefined || tabButtons == undefined || tabButtons.length == undefined) {
        return;
      }
      
      if(tabViews.length == 0) {
        return;
      }
      
      for(var i = 0; i < tabButtons.length; i++) {
        if(tabButtons[i].checked()) {
          tabViews[i].visible(true).layout();
        } else {
          tabViews[i].visible(false).layout();
        }
      }
    };
    
    this._createDom = function() {
        Base._createDom.call(this);
        
        var tabTitleRect = this.rect().clone();
        tabTitleRect.height = 25;
                
        //alert(tabTitleRect);
        tabTitleFlow = { view: 'HFlow', rect: tabTitleRect, anchors: 'left top right bottom' };
        this._tabLableBox = uki.build(tabTitleFlow)[0];
        this.appendChild(this._tabLableBox);

        var tabViewRect = this.rect().clone();
        tabViewRect.y = tabTitleRect.height;
        tabViewRect.height = tabViewRect.height - tabTitleRect.height;
        //alert(tabViewRect);

        tabViewBox = { view: 'Box', rect: tabViewRect, anchors: 'left topp right bottom', background: 'cssBox(background:#FFFFFF;border-top:1px solid #999)'}; 
            
        this._tabViewBox = uki.build(tabViewBox)[0];
        this.appendChild(this._tabViewBox);
    };
    
    /*
    this.rect = function(rect) {
        var result = Base.rect.call(this, rect);
        return result;
    };
    */
    
    this._createTabButton = function(descr) {
      var rect = this.rect().clone();
      rect.height = 25;
      return uki.extend({ 
                view: 'uki.more.view.ToggleButton', rect: rect, focusable: false, anchors: 'left top', inset: '0 15 0 15'
            }, descr);
    };  
});
