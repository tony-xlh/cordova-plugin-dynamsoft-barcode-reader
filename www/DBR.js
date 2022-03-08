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

exports.destroy = function (success, error) {
    exec(success, error, 'DBR', 'destroy', []);
};

exports.initWithOrganizationID = function (arg0, success, error) {
    exec(success, error, 'DBR', 'initWithOrganizationID', [arg0]);
};

exports.initRuntimeSettingsWithString = function (arg0, success, error) {
    exec(success, error, 'DBR', 'initRuntimeSettingsWithString', [arg0]);
};

exports.outputSettingsToString = function (success, error) {
    exec(success, error, 'DBR', 'outputSettingsToString', []);
};
