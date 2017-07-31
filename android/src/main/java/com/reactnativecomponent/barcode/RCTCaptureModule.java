package com.reactnativecomponent.barcode;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.zxing.BarcodeFormat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class RCTCaptureModule extends ReactContextBaseJavaModule {

    private ReactApplicationContext mContext;
    RCTCaptureManager captureManager;


    public RCTCaptureModule(ReactApplicationContext reactContext, RCTCaptureManager captureManager) {
        super(reactContext);
        this.mContext = reactContext;
        this.captureManager = captureManager;
    }

    @Override
    public String getName() {
        return "CaptureModule";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
            {
                put("barCodeTypes", getBarCodeTypes());
            }
            private Map<String, Object> getBarCodeTypes() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("upce", BarcodeFormat.UPC_E.toString());
                        put("code39", BarcodeFormat.CODE_39.toString());
                        put("ean13",BarcodeFormat.EAN_13.toString() );
                        put("ean8",BarcodeFormat.EAN_8.toString() );
                        put("code93", BarcodeFormat.CODE_93.toString());
                        put("code128", BarcodeFormat.CODE_128.toString());
                        put("pdf417",BarcodeFormat.PDF_417.toString() );
                        put("qr",BarcodeFormat.QR_CODE.toString() );
                        put("aztec", BarcodeFormat.AZTEC.toString());
                        put("itf14",BarcodeFormat.ITF.toString());
                        put("datamatrix", BarcodeFormat.DATA_MATRIX.toString());
                    }

                });
            }
        });
    }


    @ReactMethod
    public void startSession() {
        if (captureManager.cap != null) {
            getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                captureManager.cap.startScan();
                }
            });
        }
    }

    @ReactMethod
    public void stopSession() {
        if (captureManager.cap != null) {
            getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                captureManager.cap.stopScan();
                }
            });
        }
    }

    @ReactMethod
    public void stopFlash() {
        if (captureManager.cap != null) {
            getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                captureManager.cap.CloseFlash();
                }
            });
        }
    }

    @ReactMethod
    public void startFlash() {
        if (captureManager.cap != null) {
            getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                captureManager.cap.OpenFlash();
                }
            });
        }
    }

}
