let scanner;

async function init (success, error, args) {
    let license = args[0];
    if (this.hasOwnProperty("Dynamsoft") == false){
        var newscript = document.createElement('script');
        newscript.setAttribute('type','text/javascript');
        newscript.setAttribute('src','https://cdn.jsdelivr.net/npm/dynamsoft-javascript-barcode@8.8.7/dist/dbr.js');
        newscript.setAttribute('data-license',license);
        newscript.onload = async function(e){
            console.log("DBR JS loaded"); 
            scanner = await Dynamsoft.DBR.BarcodeScanner.createInstance();
            success(true);
        };
        document.body.appendChild(newscript);
    } else{
        if (!scanner) {
            scanner = await Dynamsoft.DBR.BarcodeScanner.createInstance();
            success(true);
        }
    }
}

async function decode(success, error, args){
    let base64 = args[0];
    if (scanner){
        let results = await scanner.decodeBase64String(base64);
        wrapResults(results);
        success(results);
    }
    error();
}

function wrapResults(results) {
    for (var i=0;i<results.length;i++){
        let result = results[i];
        result.barcodeFormat = result.barcodeFormatString;
        result.x1 = result.localizationResult.x1;
        result.x2 = result.localizationResult.x2;
        result.x3 = result.localizationResult.x3;
        result.x4 = result.localizationResult.x4;
        result.y1 = result.localizationResult.y1;
        result.y2 = result.localizationResult.y2;
        result.y3 = result.localizationResult.y3;
        result.y4 = result.localizationResult.y4;
    }
}

async function initRuntimeSettingsWithString(success, error, args){
    if (scanner){
        let template = args[0];
        await scanner.initRuntimeSettingsWithString(template);
        success();
    }
    error();
}

async function outputSettingsToString(success, error){
    if (scanner) {
        let settings = await scanner.outputSettingsToString();
        success(settings);  
    }
    error();
}

async function outputSettingsToString(success, error){
    if (scanner) {
        let settings = await scanner.outputSettingsToString();
        success(settings);  
    }
    error();
}

async function destroy(success,error) {
    if (scanner) {
        await scanner.destroyContext();
        scanner = undefined;
        success();
    }
    error();
}

async function startScanning(onScanned, error) {
    try{
        scanner.getUIElement().getElementsByClassName("dce-btn-close")[0].remove();
        scanner.getUIElement().getElementsByClassName("dbrScanner-cvs-drawarea")[0].remove();
        scanner.onFrameRead = results => {
            wrapResults(results);
            onScanned(results);
        };
        await scanner.show();
    } catch (e){
        throw e;
    }
}

async function stopScanning() {
    try{
        await scanner.hide();
    } catch (e){
        throw e;
    }
}

function pauseScanning() {
    try{
        scanner.pause();
    } catch (e){
        throw e;
    }
}

async function resumeScanning() {
    try{
        await scanner.play();
    } catch (e){
        throw e;
    }
}

module.exports = {
    init: init,
    decode: decode,
    initRuntimeSettingsWithString: initRuntimeSettingsWithString,
    outputSettingsToString: outputSettingsToString,
    destroy: destroy,
    startScanning: startScanning,
    stopScanning: stopScanning,
    pauseScanning: pauseScanning,
    resumeScanning: resumeScanning,
    cleanup: function () {}
};

require('cordova/exec/proxy').add('DBR', module.exports);