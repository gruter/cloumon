include('../../monitor_app.js');

var ZKDataLoader = function() {
};

ZKDataLoader.prototype = {
  currentData: [],
  //ZKNode 목록을 가져온다.
  getZKNodes: function(zkservers, path, callback) {
    uki.ajax({
      url: '/zkmanager?action=GetZKNodes&zkservers=' + zkservers + '&path=' + path,
      async: false,
      success: function(data) {
        if(data == '' || data == undefined) {
          data = [];
        }
        callback(data);
      }
    }); 
  }		
};