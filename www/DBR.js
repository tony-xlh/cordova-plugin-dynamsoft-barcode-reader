cordova.define("cordova-plugin-dynamsoft-barcode-reader.DBR", function(require, exports, module) {
var exec = require('cordova/exec');

exports.decode = function (arg0, success, error) {
    exec(success, error, 'DBR', 'decode', [arg0]);
};

exports.init = function (arg0, success, error) {
    exec(success, error, 'DBR', 'init', [arg0]);
};

});
