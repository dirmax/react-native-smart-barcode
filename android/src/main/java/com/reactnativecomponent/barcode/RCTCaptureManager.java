package com.reactnativecomponent.barcode;


import android.app.Activity;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.common.SystemClock;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.google.zxing.BarcodeFormat;
import com.reactnativecomponent.barcode.event.BarcodeResultEvent;
import com.reactnativecomponent.barcode.view.CaptureView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RCTCaptureManager extends ViewGroupManager<CaptureView> {

    private static final String REACT_CLASS = "CaptureView";
    public static final int CHANGE_SHOW = 0;
    CaptureView cap;


    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public CaptureView createViewInstance(ThemedReactContext context) {
        Activity activity = context.getCurrentActivity();
        cap = new CaptureView(activity, context);
        return cap;
    }

    @ReactProp(name = "barCodeTypes")
    public void setbarCodeTypes(CaptureView view, ReadableArray barCodeTypes) {

        if (barCodeTypes == null) {
            return;
        }
        List<String> result = new ArrayList<String>(barCodeTypes.size());
        for (int i = 0; i < barCodeTypes.size(); i++) {
            result.add(barCodeTypes.getString(i));
        }
        view.setDecodeFormats(result);

    }


    @ReactProp(name = "possiblePointsColor")
    public void setPossiblePointsColor(CaptureView view, String color) {
        view.setPossiblePointsColor(color);
    }

    @Override
    protected void addEventEmitters(final ThemedReactContext reactContext, final CaptureView view) {

        view.setOnEvChangeListener(new CaptureView.OnEvChangeListener() {

            @Override
            public void getQRCodeResult(String result,BarcodeFormat format) {
                reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher()
                    .dispatchEvent(new BarcodeResultEvent(view.getId(), SystemClock.nanoTime(), result,format));
            }

        });

    }

    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
            .put("QRCodeResult", MapBuilder.of("registrationName", "onBarCodeRead"))
            .build();
    }

}