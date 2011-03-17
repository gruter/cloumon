include('../view.js');

uki.view.declare('uki.view.NativeIframe', uki.view.Base, function(Base) { 
    this._createDom = function() { 
      this._dom = uki.createElement('iframe', this.defaultCss + 'border:none;left:0;top:0;width:100%;height:100%'); 
    }; 

    // access to the src attribute 
    uki.delegateProp(this, 'src', '_dom'); 
    uki.delegateProp(this, 'name', '_dom'); 

    // do not apply layout rules. Leave it 100% x 100% 
    this._layoutDom = function() {}; 

}); 

// access to the src attribute for collection 
uki.Collection.addAttrs(['src', 'name']); 