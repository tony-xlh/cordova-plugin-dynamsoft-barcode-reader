# cordova-plugin-dynamsoft-barcode-reader

## How to use

1. Install the plugin

    ```
    $ cordova plugins install <path to the plugin>
    ```

2. Decode base64-encoded image

In the JavaScript file:

    ```js
    cordova.plugins.DBR.decode(base64,callback)
    ```

## How the plugin is made

1. Use plugman to create a plugin

    ```
    $ plugman create --name DBR --plugin_id cordova-plugin-dynamsoft-barcode-reader --plugin_version 0.0.1
    ```

2. Add platform

    ```
    plugman platform add --platform_name ios
    ```

3. Implement the plugin

    Modify the `DBR.m` and `DBR.js` files.

4. Configure the framework in the `plugins.xml`

    ```
    <platform name="ios">
        <config-file parent="/*" target="config.xml">
            <feature name="DBR"><param name="ios-package" value="DBR" /></feature>
        </config-file>
        <source-file src="src/ios/DBR.m" />
        <framework src="src/ios/DynamsoftBarcodeReader.framework" embed="true" custom="true"/>
    </platform>
    ```

5. Create package.json 

    ```
    plugman createpackagejson .
    ```