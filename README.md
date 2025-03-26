# cordova-plugin-dynamsoft-barcode-reader

![version](https://img.shields.io/npm/v/cordova-plugin-dynamsoft-barcode-reader.svg)

[Dynamsoft Barcode Reader](https://www.dynamsoft.com/barcode-reader/overview/) SDK for Cordova.

## SDK Versions Used for Different Platforms

| Product      | Android |    iOS |
| ----------- | ----------- | -----------  |
| Dynamsoft Barcode Reader    | 9.6.40       | 9.6.40     |

## Supported Platforms

* Android
* iOS

## How to use

### Install the plugin

```
$ cordova plugins add cordova-plugin-dynamsoft-barcode-reader
```

Or:

```
$ cordova plugins add https://github.com/xulihang/cordova-plugin-dynamsoft-barcode-reader.git
```

### Methods

* `init`

    Initialize Dynamsoft Barcode Reader with a valid license. You can apply for a trial license [here](https://www.dynamsoft.com/customer/license/trialLicense/?product=dbr).
    
    ```js
    cordova.plugins.DBR.init(license,successCallback,errorCallback);
    ```

* `decode`

    Decode base64 and return results

    ```js
    cordova.plugins.DBR.decode(base64,successCallback,errorCallback);
    ```
    
    A result object has the following properties: `barcodeText`, `barcodeBytesBase64` `barcodeFormat` and localization results: `x1`, `y1`, `x2`, `y2`, `x3`, `y3`, `x4`, `y4`.
    
* `initRuntimeSettingsWithString`
    
    Set runtime settings with JSON template

    ```js
    cordova.plugins.DBR.initRuntimeSettingsWithString(template,onInitSettings);
    ```
    
    A sample template which specifies the barcode format as QR code:
    
    ```json
    {
      "ImageParameter": {
        "BarcodeFormatIds": [
          "BF_QR_CODE"
        ],
        "BinarizationModes": [
          {
            "BlockSizeX": 61,
            "BlockSizeY": 61,
            "LibraryFileName": "",
            "LibraryParameters": "",
            "Mode": "BM_LOCAL_BLOCK"
          }
        ],
        "Description": "",
        "ExpectedBarcodesCount": 1,
        "Name": "Settings",
        "Timeout": 99999
      },
      "Version": "3.0"
    }
    ```
    
* `outputSettingsToString`

    Output the current runtime settings to string.

    ```js
    cordova.plugins.DBR.outputSettingsToString(onOutput);
    ```
    
*  `destroy`

    Destroy the current instance of Dynamsoft Barcode Reader.

    ```js
    cordova.plugins.DBR.destroy(success, error);
    ```

*  `startScanning`

    Open the camera using Dynamsoft Camera Enhancer and decode frames. 

    ```js
    cordova.plugins.DBR.startScanning({"dceLicense":"license","resolution":2}, onScanned, error);
    ```
    
    The scan options:
    
    ```ts
    interface ScanOptions {
      dceLicense?: string;
      rotate?: boolean; //whether to convert the frame to bitmap and rotate it, false by default
      resolution?: number; // check out the following enum of resolution
    }
    
    enum EnumResolution {
      RESOLUTION_AUTO = 0,
      RESOLUTION_480P = 1,
      RESOLUTION_720P = 2,
      RESOLUTION_1080P = 3,
      RESOLUTION_2K = 4,
      RESOLUTION_4K = 5,
    }
    ```

    The `onScanned` callback will return the frame resolution, frame rotation and barcode results.

*  `stopScanning`

    Close the camera.

    ```js
    cordova.plugins.DBR.stopScanning(success, error);
    ```

*  `pauseScanning`

    Pause the camera.

    ```js
    cordova.plugins.DBR.pauseScanning(success, error);
    ```

*  `resumeScanning`

    Resume the camera.

    ```js
    cordova.plugins.DBR.resumeScanning(success, error);
    ```

*  `switchTorch`

    Turn on/off the torch. Value of desiredStatus: `on`, `off`.

    ```js
    cordova.plugins.DBR.switchTorch(desiredStatus, success, error);
    ```

*  `setZoom`

    Set the zoom factor of the camera.

    ```js
    cordova.plugins.DBR.setZoom(zoomFactor, success, error);
    ```
    
*  `setFocus`

    Set the point to focus for the camera.

    ```js
    cordova.plugins.DBR.setFocus(point, success, error);
    ```
    
    Point: `{x:number,y:number}`

*  `getResolution`

    Get the current video resolution in format like this: `1280x720`.

    ```js
    cordova.plugins.DBR.getResolution(success, error);
    ```

## Demo

* [Ionic Angular Barcode Scanner](https://github.com/xulihang/Ionic-Angular-Cordova-Barcode-Scanner)
* [Ionic React Barcode Scanner](https://github.com/xulihang/Ionic-React-Cordova-Barcode-Scanner)

## Ionic Wrapper

[@awesome-cordova-plugins/dynamsoft-barcode-scanner](https://danielsogl.gitbook.io/awesome-cordova-plugins/dynamsoft-barcode-scanner)

## License Versions

For versions >= 1.2.0, License 3 is used.

For versions < 1.2.0, License 1 and 2 are used.

## Supported Barcode Symbologies

* Code 11
* Code 39
* Code 93
* Code 128
* Codabar
* EAN-8
* EAN-13
* UPC-A
* UPC-E
* Interleaved 2 of 5 (ITF)
* Industrial 2 of 5 (Code 2 of 5 Industry, Standard 2 of 5, Code 2 of 5)
* ITF-14 
* QRCode
* DataMatrix
* PDF417
* GS1 DataBar
* Maxicode
* Micro PDF417
* Micro QR
* PatchCode
* GS1 Composite
* Postal Code
* Dot Code
* PharmaCode

## How the plugin is made

1. Use plugman to create a plugin

    ```
    $ plugman create --name DBR --plugin_id cordova-plugin-dynamsoft-barcode-reader --plugin_version 0.0.1
    ```

2. Add platform

    ```
    $ plugman platform add --platform_name ios
    ```
    
    ```
    $ plugman platform add --platform_name android
    ```

3. Implement the plugin

    Modify the `DBR.java`，`DBR.m` and `DBR.js` files. Set up the gradle and cocoapods to use the Android aar file and the iOS framework of Dynamsoft Barcode Reader.

4. Create package.json 

    ```
    $ plugman createpackagejson .
    ```
