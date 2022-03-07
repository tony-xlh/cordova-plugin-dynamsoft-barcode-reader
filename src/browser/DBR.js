let reader;

async function init (success, error, args) {
    let license = args[0];
    if (this.hasOwnProperty("Dynamsoft") == false){
        var newscript = document.createElement('script');
        newscript.setAttribute('type','text/javascript');
        newscript.setAttribute('src','https://cdn.jsdelivr.net/npm/dynamsoft-javascript-barcode@8.8.7/dist/dbr.js');
        newscript.setAttribute('data-license',license);
        newscript.onload = async function(e){
            console.log("DBR JS loaded"); 
            reader = await Dynamsoft.DBR.BarcodeReader.createInstance();
            success(true);
        };
        document.body.appendChild(newscript);
    } else{
        if (!reader) {
            reader = await Dynamsoft.DBR.BarcodeReader.createInstance();
            success(true);
        }
    }
}

async function decode(success, error, args){
    let base64 = args[0];
    if (reader){
        let results = await reader.decodeBase64String(base64);
        for (var i=0;i<results.length;i++){
          let result = results[i];
          result.x1 = result.localizationResult.x1;
          result.x2 = result.localizationResult.x2;
          result.x3 = result.localizationResult.x3;
          result.x4 = result.localizationResult.x4;
          result.y1 = result.localizationResult.y1;
          result.y2 = result.localizationResult.y2;
          result.y3 = result.localizationResult.y3;
          result.y4 = result.localizationResult.y4;
        }
        success(results);
    }
    error();
}

async function initRuntimeSettingsWithString(success, error, args){
    if (reader){
        let template = args[0];
        await reader.initRuntimeSettingsWithString(template);
        success();
    }
    error();
}

async function outputSettingsToString(success, error){
    if (reader) {
        let settings = await reader.outputSettingsToString();
        success(settings);  
    }
    error();
}


module.exports = {
    init: init,
    decode: decode,
    initRuntimeSettingsWithString: initRuntimeSettingsWithString,
    outputSettingsToString: outputSettingsToString,
    cleanup: function () {}
};

require('cordova/exec/proxy').add('DBR', module.exports);