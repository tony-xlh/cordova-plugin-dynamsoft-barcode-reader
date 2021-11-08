# cordova-plugin-dynamsoft-barcode-reader

![version](https://img.shields.io/npm/v/cordova-plugin-dynamsoft-barcode-reader.svg)

[Dynamsoft Barcode Reader](https://www.dynamsoft.com/barcode-reader/overview/) SDK for Cordova.

## Supported Platforms

* Android
* iOS

## How to use

### Install the plugin

```
$ cordova plugins install cordova-plugin-dynamsoft-barcode-reader
```

Or:

```
$ cordova plugins install https://github.com/cordova-plugin-dynamsoft-barcode-reader
```

### Methods

* `init`

    Initialize Dynamsoft Barcode Reader with an organization ID.
    
    ```js
    cordova.plugins.DBR.init(organizationID,successCallback,errorCallback);
    ```

* `decode`

    Decode base64 and return results

    ```js
    cordova.plugins.DBR.decode(base64,successCallback,errorCallback);
    ```
    
    A result object has the following properties: `barcodeText`, `barcodeFormat` and localization results: `x1`, `y1`, `x2`, `y2`, `x3`, `y3`, `x4`, `y4`.
    
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

## Supported Barcode Symbologies

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