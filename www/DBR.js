var exec = require('cordova/exec');

exports.decode = function (arg0, success, error) {
    exec(success, error, 'DBR', 'decode', [arg0]);
};

exports.init = function (arg0, success, error) {
    if (!arg0){
        arg0="DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ==";
    }
    exec(success, error, 'DBR', 'init', [arg0]);
};

exports.initRuntimeSettingsWithString = function (arg0, success, error) {
    exec(success, error, 'DBR', 'initRuntimeSettingsWithString', [arg0]);
};

exports.outputSettingsToString = function (success, error) {
    exec(success, error, 'DBR', 'outputSettingsToString', []);
};

exports.destroy = function (success, error) {
    exec(success, error, 'DBR', 'destroy', []);
};

exports.startScanning = function (arg0, onScanned, error) {
    if (!arg0){
        arg0={"dceLicense":"DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ=="};
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

exports.getResolution = function (success, error) {
    exec(success, error, 'DBR', 'getResolution', []);
};

exports.switchTorch = function (arg0, success, error) {
    exec(success, error, 'DBR', 'switchTorch', [arg0]);
};

exports.setZoom = function (arg0, success, error) {
    exec(success, error, 'DBR', 'setZoom', [arg0]);
};

exports.setFocus = function (arg0, success, error) {
    exec(success, error, 'DBR', 'setFocus', [arg0]);
};


