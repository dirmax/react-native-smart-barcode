package com.reactnativecomponent.barcode.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.reactnativecomponent.barcode.R;
import com.reactnativecomponent.barcode.camera.CameraManager;
import com.reactnativecomponent.barcode.decoding.CaptureActivityHandler;

import java.io.IOException;
import java.util.List;
import java.util.Vector;


public class CaptureView extends FrameLayout implements TextureView.SurfaceTextureListener {

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private MediaPlayer mediaPlayer;
    private boolean playBeep=true;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private Activity activity;
    private ViewGroup.LayoutParams param;
    private int ScreenWidth, ScreenHeight;
    private TextureView textureView;
    private int height;
    private int width;
    public boolean decodeFlag = true;
    private Handler mHandler;
    private int cX;
    private int cY;
    private String possiblePointsColor;
    private int CORNER_COLOR = Color.GREEN;
    private int Min_Frame_Width;
    private int MAX_FRAME_WIDTH;
    private int MAX_FRAME_HEIGHT;
    private float density;
    public int scanTime = 1000;
    private int focusTime = 1000;
    private int zoomLevel = 20;

    public OnEvChangeListener onEvChangeListener;

    SurfaceTexture surfaceTexture;
    boolean autoStart = true;


    public CaptureView(Activity activity, Context context) {
        super(context);
        this.activity = activity;
        CameraManager.init(activity.getApplication());
        param = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        density = context.getResources().getDisplayMetrics().density;
        Min_Frame_Width = (int) (100 * density + 0.5f);
        Resources resources = activity.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        ScreenWidth = dm.widthPixels;
        ScreenHeight = dm.heightPixels;

        hasSurface = false;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        height = getHeight();
        width = getWidth();
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }


    private void initCameraManager() {

        CameraManager.get().x = cX + width;
        CameraManager.get().y = cY + height;
        CameraManager.get().MIN_FRAME_WIDTH = MAX_FRAME_WIDTH;
        CameraManager.get().MIN_FRAME_HEIGHT = MAX_FRAME_HEIGHT;
        CameraManager.get().MAX_FRAME_WIDTH = MAX_FRAME_WIDTH;
        CameraManager.get().MAX_FRAME_HEIGHT = MAX_FRAME_HEIGHT;
        CameraManager.get().setFocusTime(focusTime);
        CameraManager.get().setZoomLevel(zoomLevel);
    }

    /**
     * Activity onResume后调用view的onAttachedToWindow
     */
    @Override
    protected void onAttachedToWindow() {
        init();
        super.onAttachedToWindow();
    }

    /**
     * surfaceview 扫码框 声音管理
     */
    private void init() {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        textureView = new TextureView(activity);
        textureView.setLayoutParams(param);
        textureView.getLayoutParams().height = ScreenHeight;
        textureView.getLayoutParams().width = ScreenWidth;
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        if (hasSurface) {
            initCamera(surfaceTexture);
        } else {
            textureView.setSurfaceTextureListener(this);

        }
        this.addView(textureView);
        viewfinderView = new ViewfinderView(activity);
        viewfinderView.setLayoutParams(param);
        viewfinderView.getLayoutParams().height = ScreenHeight;
        viewfinderView.getLayoutParams().width = ScreenWidth;
        viewfinderView.setBackgroundColor(getResources().getColor(R.color.transparent));
        viewfinderView.setPossiblePointsColor(this.possiblePointsColor);
        this.addView(viewfinderView);

//        decodeFormats = null;
        characterSet = null;

        vibrate = true;

        setPlayBeep(true);

    }

    public void setPlayBeep(boolean b) {
        playBeep = b;
        AudioManager audioService = (AudioManager) activity.getSystemService(activity.AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
           playBeep = false;
        }
        initBeepSound();
    }

    public void startScan() {

        this.decodeFlag = true;

        if (!hasSurface) {
            hasSurface = true;
            CameraManager.get().framingRectInPreview = null;
            initCamera(surfaceTexture);

            CameraManager.get().initPreviewCallback();
            CameraManager.get().startPreview();

        }
//        decodeFormats = null;

        handler = new CaptureActivityHandler(this, decodeFormats,
                characterSet, this.getViewfinderView());
//        handler.restartPreviewAndDecode();
    }

    public void stopScan() {
        hasSurface = false;
        if (handler != null) {
            handler.quitSynchronously();
        }
        CameraManager.get().stopPreview();
        CameraManager.get().closeDriver();
    }

    public void stopQR() {
        this.decodeFlag = false;
    }

    public void startQR() {
        this.decodeFlag = true;
        startScan();
    }

    /**
     * ondestroy调用,会执行onDetachedFromWindow
     */

    @Override
    protected void onDetachedFromWindow() {
        this.removeView(viewfinderView);
        this.removeView(textureView);

        if (handler != null) {
            handler.quitSynchronously();
        }

        super.onDetachedFromWindow();
    }


    private void initCamera(SurfaceTexture surfaceTexture) {
        try {
            CameraManager.get().openDriver(surfaceTexture);


        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        //if (handler == null) {
        //    handler = new CaptureActivityHandler(this, decodeFormats,
         //           characterSet, this.getViewfinderView());
        //}
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }


    public void handleDecode(Result obj, Bitmap barcode) {

//        viewfinderView.drawResultBitmap(barcode);//画结果图片

        if (obj != null&& this.decodeFlag) {
            playBeepSoundAndVibrate();
            String str = obj.getText();//获得扫码的结果
        /*
        activity.getCapturePackage().mModuleInstance.sendMsgToRn(str); //发送到RN侧*/
            onEvChangeListener.getQRCodeResult(str,obj.getBarcodeFormat()); //观察者模式发送到RN侧
        }
        stopQR();


//        viewfinderView.drawResultBitmap(null);//清除结果图片

        /*
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initCamera(textureView.getHolder());
        if (handler != null) {
            handler.restartPreviewAndDecode();
        }*/

    }


    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {

        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }

        if (vibrate) {
            Vibrator vibrator = (Vibrator) activity.getSystemService(activity.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }

    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.seekTo(0);
        }
    };


    public Activity getActivity() {
        return activity;
    }


    public void setHandler(CaptureActivityHandler handler) {
        this.handler = handler;

    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }


    public void setFocusTime(int focusTime) {
        this.focusTime = focusTime;
    }


    public void setcX(int cX) {

        if (width != 0 && ((cX > width / 2 - Min_Frame_Width) || cX < (Min_Frame_Width - width / 2))) {
            if (cX > 0) {
                cX = width / 2 - Min_Frame_Width;
            } else {
                cX = Min_Frame_Width - width / 2;
            }
        }

        this.cX = cX;
        CameraManager.get().x = cX + width;
        CameraManager.get().framingRect = null;
        if (viewfinderView != null) {
            viewfinderView.invalidate();
        }

    }

    public void setcY(int cY) {


        if (height != 0 && ((cY > height / 2 - Min_Frame_Width) || cY < (Min_Frame_Width - height / 2))) {
            if (cY > 0) {
                cY = height / 2 - Min_Frame_Width;
            } else {
                cY = Min_Frame_Width - height / 2;
            }
        }

        this.cY = cY;
        CameraManager.get().y = cY + height;
        CameraManager.get().framingRect = null;
        if (viewfinderView != null) {
            viewfinderView.invalidate();
        }

    }

    public void setMAX_FRAME_WIDTH(int MAX_FRAME_WIDTH) {
        if (width != 0 && MAX_FRAME_WIDTH > width) {
            MAX_FRAME_WIDTH = width;
        }
        this.MAX_FRAME_WIDTH = MAX_FRAME_WIDTH;
        CameraManager.get().MIN_FRAME_WIDTH = this.MAX_FRAME_WIDTH;

        CameraManager.get().MAX_FRAME_WIDTH = this.MAX_FRAME_WIDTH;
        CameraManager.get().framingRect = null;
        if (viewfinderView != null) {
            viewfinderView.invalidate();
        }

    }

    public void setMAX_FRAME_HEIGHT(int MAX_FRAME_HEIGHT) {

        if (height != 0 && MAX_FRAME_HEIGHT > height) {
            MAX_FRAME_HEIGHT = width;
        }
        this.MAX_FRAME_HEIGHT = MAX_FRAME_HEIGHT;

        CameraManager.get().MIN_FRAME_HEIGHT = this.MAX_FRAME_HEIGHT;

        CameraManager.get().MAX_FRAME_HEIGHT = this.MAX_FRAME_HEIGHT;
        CameraManager.get().framingRect = null;
        if (viewfinderView != null) {
            viewfinderView.invalidate();
        }

    }

    public void setPossiblePointsColor(String color) {
        this.possiblePointsColor = color;
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {

        super.onWindowFocusChanged(hasWindowFocus);

        if (hasWindowFocus) {
            //对应onresume
            this.surfaceTexture = textureView.getSurfaceTexture();
            startScan();
        } else {
            //对应onpause
            stopScan();
        }

    }

    /**
     * 开启闪光灯常亮
     */
    public void OpenFlash(){
        try {
            Camera.Parameters param =CameraManager.get().getCamera().getParameters();

            param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

            CameraManager.get().getCamera().setParameters(param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }  /**
     * 关闭闪光灯常亮
     */
    public void CloseFlash(){
        try {
            Camera.Parameters param = CameraManager.get().getCamera().getParameters();

            param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);


            CameraManager.get().getCamera().setParameters(param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        CameraManager.init(activity);

        initCameraManager();

        surfaceTexture = surface;

        if (autoStart) {
            startScan();
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stopScan();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }



    public Vector<BarcodeFormat> getDecodeFormats() {
        return decodeFormats;
    }

    public void setDecodeFormats(List<String> decode) {
        decodeFormats=new Vector<BarcodeFormat>();
        for(BarcodeFormat format : BarcodeFormat.values()){
            if(decode.contains(format.toString())){
                decodeFormats.add(format);
            }
        }

    }

    public interface OnEvChangeListener {
        public void getQRCodeResult(String result, BarcodeFormat format);
    }

    public void setOnEvChangeListener(OnEvChangeListener onEvChangeListener) {
        this.onEvChangeListener = onEvChangeListener;
    }


    public String ShowResult(Intent intent) {
        return intent.getData().toString();
    }

}


