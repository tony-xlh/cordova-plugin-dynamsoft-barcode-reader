package com.dynamsoft.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.DBRDLSLicenseVerificationListener;
import com.dynamsoft.dbr.DMDLSConnectionParameters;
import com.dynamsoft.dbr.TextResult;

/**
 * This class echoes a string called from JavaScript.
 */
public class DBR extends CordovaPlugin {
    private BarcodeReader barcodeReader;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("init")) {
            String message = args.getString(0);
            this.init(message, callbackContext);
            return true;
        }
        if (action.equals("decode")) {
            String message = args.getString(0);
            this.decode(message, callbackContext);
            return true;
        }
        return false;
    }

    private void init(String organizationID, CallbackContext callbackContext) {
        try {
            initDBR(organizationID);
            callbackContext.success();
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }
    
    private void initDBR(String organizationID) throws BarcodeReaderException {
        barcodeReader = new BarcodeReader();
        DMDLSConnectionParameters dbrParameters = new DMDLSConnectionParameters();
        dbrParameters.organizationID = organizationID;
        barcodeReader.initLicenseFromDLS(dbrParameters, new DBRDLSLicenseVerificationListener() {
            @Override
            public void DLSLicenseVerificationCallback(boolean isSuccessful, Exception e) {
                if (!isSuccessful) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void decode(String base64, CallbackContext callbackContext) {
         cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    JSONArray results = decodeBase64(base64);
                    callbackContext.success(results); // Thread-safe.
                } catch (Exception e) {
                    e.printStackTrace();
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }
    
    private JSONArray decodeBase64(String base64) throws BarcodeReaderException, JSONException {
        JSONArray decodingResults = new JSONArray();
        TextResult[] results = barcodeReader.decodeBase64String(base64, "");
        for (TextResult result : results) {

            JSONObject decodingResult = new JSONObject();
            decodingResult.put("barcodeText", result.barcodeText);
            decodingResult.put("barcodeFormat", result.barcodeFormatString);
            decodingResult.put("x1", result.localizationResult.resultPoints[0].x);
            decodingResult.put("y1", result.localizationResult.resultPoints[0].y);
            decodingResult.put("x2", result.localizationResult.resultPoints[1].x);
            decodingResult.put("y2", result.localizationResult.resultPoints[1].y);
            decodingResult.put("x3", result.localizationResult.resultPoints[2].x);
            decodingResult.put("y3", result.localizationResult.resultPoints[2].y);
            decodingResult.put("x4", result.localizationResult.resultPoints[3].x);
            decodingResult.put("y4", result.localizationResult.resultPoints[3].y);
            decodingResults.put(decodingResult);
        }
        return decodingResults;
    }
    
}
