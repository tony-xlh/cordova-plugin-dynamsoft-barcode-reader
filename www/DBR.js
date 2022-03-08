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

exports.startScanning = function (arg0, onScanned, error) {
    if (!arg0){
        arg0="DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9";
    }
    exec(onScanned, error, 'DBR', 'startScanning', [arg0]);
};

exports.stopScanning = function (success, error) {
    exec(success, error, 'DBR', 'stopScanning', []);
};

exports.pauseScanning = function (success, error) {
    exec(success, error, 'DBR', 'pauseScanning', []);
};

exports.resumeScanning = function (success, error) {
    exec(success, error, 'DBR', 'resumeScanning', []);
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
