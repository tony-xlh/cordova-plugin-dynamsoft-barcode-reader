var exec = require('cordova/exec');

exports.decode = function (arg0, success, error) {
    exec(success, error, 'DBR', 'decode', [arg0]);
};

exports.init = function (arg0, success, error) {
    if (!arg0){
        arg0="200001";
    }
    exec(success, error, 'DBR', 'init', [arg0]);
};

