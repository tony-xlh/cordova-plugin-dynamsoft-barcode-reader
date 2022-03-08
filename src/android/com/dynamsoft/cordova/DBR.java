package com.dynamsoft.cordova;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.DBRDLSLicenseVerificationListener;

import com.dynamsoft.dbr.DMDLSConnectionParameters;
import com.dynamsoft.dbr.EnumConflictMode;

import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraEnhancerException;
import com.dynamsoft.dce.DCECameraView;
import com.dynamsoft.dce.DCEFrame;
import com.dynamsoft.dce.DCEFrameListener;
import com.dynamsoft.dce.DCELicenseVerificationListener;


/**
 * This class echoes a string called from JavaScript.
 */
public class DBR extends CordovaPlugin {
    private BarcodeReader barcodeReader;
    private CameraEnhancer mCameraEnhancer = null;
    private DCECameraView mCameraView = null;
    private CallbackContext startCameraCallbackContext;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("init")) {
            String message = args.getString(0);
            this.init(message, callbackContext);
            return true;
        }else if (action.equals("initWithOrganizationID")) {
            String message = args.getString(0);
            this.initWithOrganizationID(message, callbackContext);
            return true;
        }else if (action.equals("decode")) {
            String message = args.getString(0);
            this.decode(message, callbackContext);
            return true;
        }else if (action.equals("outputSettingsToString")) {
            try {
                String settings = barcodeReader.outputSettingsToString("currentRuntimeSettings");
                callbackContext.success(settings);
            } catch (BarcodeReaderException e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }
            return true;
        }else if (action.equals("initRuntimeSettingsWithString")) {
            String template = args.getString(0);
            try {
                System.out.println(template);
                barcodeReader.initRuntimeSettingsWithString(template, EnumConflictMode.CM_OVERWRITE);
                callbackContext.success();
            } catch (BarcodeReaderException e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }
            return true;
        }else if (action.equals("destroy")) {
            try{
                startCameraCallbackContext = null;
                barcodeReader.destroy();
                callbackContext.success();
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }
            return true;
        }else if (action.equals("startScanning")) {
            String dceLicense = args.getString(0);
            try{
                startScanning(dceLicense, callbackContext);
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }
            return true;
        }else if (action.equals("stopScanning")) {
            try{
                stopScanning();
                callbackContext.success();
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }
            return true;
        }else if (action.equals("pauseScanning")) {
            if (mCameraEnhancer != null) {
                try {
                    mCameraEnhancer.pause();
                    callbackContext.success();
                } catch (CameraEnhancerException e) {
                    e.printStackTrace();
                    callbackContext.error(e.getMessage());
                }
            }else{
                callbackContext.error("not started");
            }
            return true;
        }else if (action.equals("resumeScanning")) {
            if (mCameraEnhancer != null) {
                try {
                    mCameraEnhancer.resume();
                    callbackContext.success();
                } catch (CameraEnhancerException e) {
                    e.printStackTrace();
                    callbackContext.error(e.getMessage());
                }
            }else{
                callbackContext.error("not started");
            }
            return true;
        }
        return false;
    }

    private void init(String license, CallbackContext callbackContext) {
        try {
            barcodeReader = new BarcodeReader();
            barcodeReader.initLicense(license);
            callbackContext.success();
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    private void initWithOrganizationID(String organizationID, CallbackContext callbackContext) {
        try {
            initDBRWithOrganizationID(organizationID);
            callbackContext.success();
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    private void initDBRWithOrganizationID(String organizationID) throws BarcodeReaderException {
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
        TextResult[] results = barcodeReader.decodeBase64String(base64, "");
        return wrapResults(results);
    }

    private JSONArray wrapResults(TextResult[] results) throws JSONException {
        JSONArray decodingResults = new JSONArray();
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

    private void startScanning(String license, CallbackContext callbackContext){
        startCameraCallbackContext = callbackContext;
        if (mCameraEnhancer == null) {
            Log.d("DBR","start scanning");
            initDCEAndStartScanning(license);
        } else{
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    makeWebViewTransparent();
                    try {
                        mCameraEnhancer.open();
                    } catch (CameraEnhancerException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void stopScanning() throws Exception {
        if (mCameraEnhancer != null) {
            mCameraEnhancer.close();
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    restoreWebViewBackground();
                }
            });
        } else{
            throw new Exception("not started");
        }
    }

    private void initDCEAndStartScanning(String license){
        if (!license.equals("")) {
            CameraEnhancer.initLicense(license, new DCELicenseVerificationListener() {
                @Override
                public void DCELicenseVerificationCallback(boolean isSuccess, Exception error) {
                    if(!isSuccess){
                        error.printStackTrace();
                    }
                }
            });
        }

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCameraEnhancer = new CameraEnhancer(cordova.getActivity());
                mCameraView = new DCECameraView(cordova.getActivity());
                mCameraEnhancer.setCameraView(mCameraView);
                mCameraView.setOverlayVisible(true);
                FrameLayout.LayoutParams cameraPreviewParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
                View view = webView.getView();
                ((ViewGroup) view.getParent()).addView(mCameraView,cameraPreviewParams);
                view.bringToFront();
                makeWebViewTransparent();
                bindDBRandDCE();
                try {
                    mCameraEnhancer.open();
                } catch (CameraEnhancerException e) {
                    Log.d("DBR",e.getMessage());
                }
            }
        });

    }

    private void bindDBRandDCE(){
        DCEFrameListener listener = new DCEFrameListener(){
            @Override
            public void frameOutputCallback(DCEFrame frame, long timeStamp) {
                //perform custom action on generated frame
                try {
                    TextResult[] textResults = barcodeReader.decodeBuffer(frame.getImageData(),frame.getWidth(),frame.getHeight(),frame.getStrides()[0], frame.getPixelFormat(),"");
                    Log.d("DBR","Found "+textResults.length+" barcode(s).");
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, wrapResults(textResults));
                    pluginResult.setKeepCallback(true);
                    startCameraCallbackContext.sendPluginResult(pluginResult);
                } catch (JSONException | BarcodeReaderException e) {
                    e.printStackTrace();
                }
            }
        };
        mCameraEnhancer.addListener(listener);
    }

    private void makeWebViewTransparent(){
        View view = webView.getView();
        view.setTag(view.getBackground());
        view.setBackgroundColor(Color.TRANSPARENT);
    }

    private void restoreWebViewBackground(){
        View view = webView.getView();
        view.setBackground((Drawable) view.getTag());
    }

}
