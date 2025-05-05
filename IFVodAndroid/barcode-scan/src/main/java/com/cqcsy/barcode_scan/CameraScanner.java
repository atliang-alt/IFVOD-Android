package com.cqcsy.barcode_scan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.cqcsy.barcode_scan.analyser.BarcodeAnalyser;
import com.cqcsy.barcode_scan.callback.OnScanResultCallback;
import com.cqcsy.barcode_scan.manager.AmbientLightManager;
import com.cqcsy.barcode_scan.manager.BeepManager;
import com.cqcsy.barcode_scan.manager.MNScanConfig;
import com.cqcsy.barcode_scan.utils.CameraSizeUtils;
import com.cqcsy.barcode_scan.utils.PointUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.List;
import java.util.concurrent.Executors;

public class CameraScanner {

    private static final int HOVER_TAP_TIMEOUT = 150;
    private static final int HOVER_TAP_SLOP = 20;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private LifecycleOwner mLifecycleOwner;
    private Camera mCamera;
    private long mLastHoveTapTime;
    private boolean isClickTap;
    private float mDownX;
    private float mDownY;
    private PreviewView mPreviewView;
    private final Context mContext;
    private BarcodeAnalyser barcodeAnalyser;
    private OnScanResultCallback onScanResultCallback;
    private MNScanConfig scanConfig;
    private BeepManager mBeepManager;
    private AmbientLightManager mAmbientLightManager;
    private View flashlightView;
    private long mLastAutoZoomTime;
    private int mScreenWidth;
    private int mScreenHeight;

    public CameraScanner(FragmentActivity activity, PreviewView previewView) {
        this.mContext = activity;
        mLifecycleOwner = activity;
        this.mPreviewView = previewView;
        initData();
    }

    public CameraScanner(Fragment fragment, PreviewView previewView) {
        this.mContext = fragment.getContext();
        mLifecycleOwner = fragment.getViewLifecycleOwner();
        this.mPreviewView = previewView;
        initData();
    }

    public BarcodeAnalyser getBarcodeAnalyser() {
        return barcodeAnalyser;
    }

    public void setOnScanResultCallback(OnScanResultCallback callback) {
        this.onScanResultCallback = callback;
    }

    public void setScanConfig(MNScanConfig config) {
        scanConfig = config;
        mBeepManager.setPlayBeep(scanConfig.isShowBeep());
        mBeepManager.setSoundRawRes(scanConfig.getBeepSoundRaw());
        mBeepManager.setVibrate(scanConfig.isShowVibrate());
    }

    public void setAnalyze(boolean analyze) {
        barcodeAnalyser.setAnalyze(analyze);
    }

    private void initData() {
        mBeepManager = new BeepManager(mContext);
        mAmbientLightManager = new AmbientLightManager(mContext);
        mAmbientLightManager.register();
        mAmbientLightManager.setOnLightSensorEventListener((dark, lightLux) -> {
            if (flashlightView != null) {
                if (dark) {
                    if (flashlightView.getVisibility() != View.VISIBLE) {
                        flashlightView.setVisibility(View.VISIBLE);
                        flashlightView.setSelected(true);
                    }
                } else if (flashlightView.getVisibility() == View.VISIBLE) {
                    flashlightView.setVisibility(View.INVISIBLE);
                    flashlightView.setSelected(false);
                }

            }
        });
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;

        initBarcodeAnalyser();
        initScaleGesture();
    }

    public void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(mContext);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                //绑定预览
                preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
                //使用后置相机
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                //配置图片扫描
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(CameraSizeUtils.getSize(mContext))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), barcodeAnalyser);
                if (mCamera != null) {
                    cameraProviderFuture.get().unbindAll();
                }
                //将相机绑定到当前控件的生命周期
                mCamera = cameraProvider.bindToLifecycle(mLifecycleOwner, cameraSelector, imageAnalysis, preview);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(mContext));
    }

    private void initBarcodeAnalyser() {
        barcodeAnalyser = new BarcodeAnalyser();
        barcodeAnalyser.setPreviewView(mPreviewView);
        barcodeAnalyser.setAnalyze(true);
        barcodeAnalyser.setOnCameraAnalyserCallback(this::handleAnalyzeResult);
    }

    private void handleAnalyzeResult(Bitmap bitmap, List<Barcode> barcodes) {

        if (scanConfig.isSupportAutoZoom() && mLastAutoZoomTime + 100 < System.currentTimeMillis()) {
            float maxDistance = Math.min(mScreenWidth, mScreenHeight);
            for (Barcode barcode : barcodes) {
                Point[] points = barcode.getCornerPoints();
                if (points != null && points.length >= 2) {
                    float distance1 = PointUtils.distance(points[0], points[1]);
                    maxDistance = distance1;
                    if (points.length >= 3) {
                        float distance2 = PointUtils.distance(points[1], points[2]);
                        float distance3 = PointUtils.distance(points[0], points[2]);
                        maxDistance = Math.max(maxDistance, Math.max(Math.max(distance1, distance2), distance3));
                    }
                }
            }
            if (handleAutoZoom((int) maxDistance, bitmap, barcodes)) {
                return;
            }
        }

        scanResultCallback(bitmap, barcodes);
    }

    private boolean handleAutoZoom(int distance, Bitmap bitmap, List<Barcode> barcodes) {
        int size = Math.min(mScreenWidth, mScreenHeight);
        if (distance * 4 < size) {
            mLastAutoZoomTime = System.currentTimeMillis();
            zoomIn();
            scanResultCallback(bitmap, barcodes);
            return true;
        }
        return false;
    }

    private void scanResultCallback(Bitmap bitmap, List<Barcode> barcodes) {
        if (onScanResultCallback != null && onScanResultCallback.onSuccess(bitmap, barcodes)) {
            if (mBeepManager != null) {
                mBeepManager.playBeepSoundAndVibrate();
            }
        }
    }

    private void initScaleGesture() {
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(mContext, mOnScaleGestureListener);
        mPreviewView.setOnTouchListener((view, event) -> {
            handlePreviewViewClickTap(event);
            if (scanConfig != null && scanConfig.isSupportZoom()) {
                return scaleGestureDetector.onTouchEvent(event);
            }
            return false;
        });
    }

    private final ScaleGestureDetector.OnScaleGestureListener mOnScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            if (mCamera != null) {
                ZoomState value = mCamera.getCameraInfo().getZoomState().getValue();
                if (value != null) {
                    float ratio = value.getZoomRatio();
                    zoomTo(ratio * scale);
                }
            }

            return true;
        }

    };

    private void handlePreviewViewClickTap(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isClickTap = true;
                    mDownX = event.getX();
                    mDownY = event.getY();
                    mLastHoveTapTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE:
                    isClickTap = distance(mDownX, mDownY, event.getX(), event.getY()) < HOVER_TAP_SLOP;
                    break;
                case MotionEvent.ACTION_UP:
                    if (isClickTap && mLastHoveTapTime + HOVER_TAP_TIMEOUT > System.currentTimeMillis()) {
                        startFocusAndMetering(event.getX(), event.getY());
                    }
                    break;
            }
        }
    }

    private float distance(float aX, float aY, float bX, float bY) {
        float xDiff = aX - bX;
        float yDiff = aY - bY;
        return (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    public void zoomTo(float ratio) {
        if (mCamera != null) {
            ZoomState zoomState = mCamera.getCameraInfo().getZoomState().getValue();
            float maxRatio = zoomState.getMaxZoomRatio();
            float minRatio = zoomState.getMinZoomRatio();
            float zoom = Math.max(Math.min(ratio, maxRatio), minRatio);
            mCamera.getCameraControl().setZoomRatio(zoom);
        }
    }

    public void zoomIn() {
        if (mCamera != null) {
            float ratio = mCamera.getCameraInfo().getZoomState().getValue().getZoomRatio() + 0.5f;
            float maxRatio = mCamera.getCameraInfo().getZoomState().getValue().getMaxZoomRatio();
            if (ratio <= maxRatio) {
                mCamera.getCameraControl().setZoomRatio(ratio);
            }
        }
    }

    public void zoomOut() {
        if (mCamera != null) {
            float ratio = mCamera.getCameraInfo().getZoomState().getValue().getZoomRatio() - 0.5f;
            float minRatio = mCamera.getCameraInfo().getZoomState().getValue().getMinZoomRatio();
            if (ratio >= minRatio) {
                mCamera.getCameraControl().setZoomRatio(ratio);
            }
        }
    }

    private void startFocusAndMetering(float x, float y) {
        if (mCamera != null) {
            MeteringPoint point = mPreviewView.getMeteringPointFactory().createPoint(x, y);
            mCamera.getCameraControl().startFocusAndMetering(new FocusMeteringAction.Builder(point).build());
        }
    }

    public void bindFlashlightView(@Nullable View v) {
        flashlightView = v;
        if (mAmbientLightManager != null) {
            mAmbientLightManager.setLightSensorEnabled(v != null);
        }
    }

    public void openLight() {
        if (mCamera != null) {
            mCamera.getCameraControl().enableTorch(true);
        }
    }

    public void closeLight() {
        if (mCamera != null) {
            mCamera.getCameraControl().enableTorch(false);
        }
    }

    public void stopCamera() {
        try {
            if (cameraProviderFuture != null) {
                cameraProviderFuture.get().unbindAll();
            }
        } catch (Exception e) {
        }
    }

    public void release() {
        try {
            mPreviewView = null;
            mLifecycleOwner = null;
            stopCamera();
        } catch (Exception e) {
        }
    }

} 