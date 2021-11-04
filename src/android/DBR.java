package cordova-plugin-dynamsoft-barcode-reader;

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
        initDBR(organizationID);
        callbackContext.success();
    }
    
    private void initDBR(String organizationID) {
        try {
            barcodeReader = new BarcodeReader();
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
        DMDLSConnectionParameters dbrParameters = new DMDLSConnectionParameters();
        dbrParameters.organizationID = "200001";
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
        JSONArray results = decodeBase64(base64);
        callbackContext.success(results);
    }
    
    private JSONArray decodeBase64(String base64) {
        JSONArray decodingResults = new JSONArray();
         
        try {
            TextResult[] results = barcodeReader.decodeBase64String(base64, "");
            System.out.println(results[0].barcodeText);
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
        } catch (BarcodeReaderException | JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return decodingResults;
    }
    
}
