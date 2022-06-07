package com.dynamsoft.cordova;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Base64;
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

import com.dynamsoft.dbr.DBRLicenseVerificationListener;
import com.dynamsoft.dbr.EnumConflictMode;

import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraEnhancerException;
import com.dynamsoft.dce.DCECameraView;
import com.dynamsoft.dce.DCEFrame;
import com.dynamsoft.dce.DCEFrameListener;
import com.dynamsoft.dce.DCELicenseVerificationListener;
import com.dynamsoft.dce.EnumResolution;


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
                barcodeReader = null;
                if (mCameraEnhancer !=null) {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ViewGroup) webView.getView().getParent()).removeView(mCameraView);
                            mCameraView = null;
                            mCameraEnhancer = null;
                        }
                    });
                }
                callbackContext.success();
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }
            return true;
        }else if (action.equals("startScanning")) {
            String options = args.getString(0);
            JSONObject jObject = new JSONObject(options);
            String dceLicense = "";
            Integer resolution = 0;
            if (jObject.has("dceLicense")) {
                dceLicense = jObject.getString("dceLicense");
            }
            if (jObject.has("resolution")) {
                resolution = jObject.getInt("resolution");
            }
            try{
                startScanning(dceLicense, resolution, callbackContext);
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
        }else if (action.equals("getResolution")){
            getResolution(callbackContext);
            return true;
        }else if (action.equals("switchTorch")){
            String desiredStatus = args.getString(0);
            Log.d("DBR", "desired status: "+desiredStatus);
            try{
                if (desiredStatus.equals("on")){
                    mCameraEnhancer.turnOnTorch();
                }else{
                    mCameraEnhancer.turnOffTorch();
                }
            }catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
            callbackContext.success();
            return true;
        }
        return false;
    }

    private void init(String license, CallbackContext callbackContext) {
        BarcodeReader.initLicense(license, new DBRLicenseVerificationListener() {
            @Override
            public void DBRLicenseVerificationCallback(boolean isSuccessful, Exception e) {
                if (!isSuccessful) {
                    e.printStackTrace();
                }
            }
        });
        try {
            barcodeReader = new BarcodeReader();
            callbackContext.success();
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
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
        TextResult[] results = barcodeReader.decodeBase64String(base64);
        return wrapResults(results);
    }

    private JSONArray wrapResults(TextResult[] results) throws JSONException {
        JSONArray decodingResults = new JSONArray();
        for (TextResult result : results) {
            JSONObject decodingResult = new JSONObject();
            decodingResult.put("barcodeText", result.barcodeText);
            decodingResult.put("barcodeFormat", result.barcodeFormatString);
            decodingResult.put("barcodeBytesBase64", Base64.encodeToString(result.barcodeBytes, Base64.DEFAULT));
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

    private void startScanning(String license, Integer resolution, CallbackContext callbackContext){
        startCameraCallbackContext = callbackContext;
        if (mCameraEnhancer == null) {
            Log.d("DBR","start scanning");
            initDCEAndStartScanning(license,resolution);
        } else{
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    makeWebViewTransparent();
                    try {
                        mCameraEnhancer.setResolution(EnumResolution.fromValue(resolution));
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

    private void initDCEAndStartScanning(String license,Integer resolution){
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
                    mCameraEnhancer.setResolution(EnumResolution.fromValue(resolution));
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
                try {
                    Bitmap rotatedBitmap = BitmapUtils.rotateBitmap(frame.toBitmap(),frame.getOrientation(),false,false);
                    TextResult[] textResults = barcodeReader.decodeBufferedImage(rotatedBitmap);
                    Log.d("DBR","Found "+textResults.length+" barcode(s).");
                    JSONObject scanResult = new JSONObject();
                    scanResult.put("frameWidth",rotatedBitmap.getWidth());
                    scanResult.put("frameHeight",rotatedBitmap.getHeight());
                    scanResult.put("results",wrapResults(textResults));
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, scanResult);
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

    private void getResolution(CallbackContext callbackContext){
        if (mCameraEnhancer != null){
            callbackContext.success(mCameraEnhancer.getResolution().getWidth()+"x"+mCameraEnhancer.getResolution().getHeight());
        }else{
            callbackContext.error("not started");
        }
    }

    @Override
    public void onPause(boolean multitasking){
        if (mCameraEnhancer != null) {
            try {
                Log.d("DBR","pause");
                mCameraEnhancer.close();
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
            }
        }
        super.onPause(multitasking);
    }
    @Override
    public void onResume(boolean multitasking){
        if (mCameraEnhancer != null) {
            try {
                Log.d("DBR","resume");
                mCameraEnhancer.open();
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
            }
        }
        super.onResume(multitasking);
    }
}
