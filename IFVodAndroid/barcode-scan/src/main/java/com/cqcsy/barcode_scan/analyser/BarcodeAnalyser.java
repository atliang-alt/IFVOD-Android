package com.cqcsy.barcode_scan.analyser;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;

import com.cqcsy.barcode_scan.callback.OnCameraAnalyserCallback;
import com.cqcsy.barcode_scan.utils.ImageProxyUtils;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

/**
 * @author : maning
 * @date : 8/18/21
 * @desc :
 */
public class BarcodeAnalyser implements ImageAnalysis.Analyzer {

    private OnCameraAnalyserCallback onCameraAnalyserCallback;

    private PreviewView mPreviewView;
    private final BarcodeScanner barcodeScanner;
    private volatile boolean isAnalyze = true;

    public BarcodeAnalyser() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);
    }

    public void setOnCameraAnalyserCallback(OnCameraAnalyserCallback onCameraAnalyserCallback) {
        this.onCameraAnalyserCallback = onCameraAnalyserCallback;
    }

    public void setAnalyze(boolean analyze) {
        isAnalyze = analyze;
    }

    public boolean isAnalyze() {
        return isAnalyze;
    }

    public void setPreviewView(PreviewView mPreviewView) {
        this.mPreviewView = mPreviewView;
    }

    private Bitmap cropBitmap(Bitmap bitmap, int cropWidth, int cropHeight) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        return Bitmap.createBitmap(bitmap, (w - cropWidth) / 2, (h - cropHeight) / 2, cropWidth, cropHeight, null, false);
    }

    @Override
    public void analyze(@NonNull final ImageProxy imageProxy) {
        Bitmap bitmap = null;
        try {
            bitmap = ImageProxyUtils.imageProxyToBitmap(imageProxy, imageProxy.getImageInfo().getRotationDegrees());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bitmap == null) {
            return;
        }
        if (mPreviewView != null) {
            //bitmap转为16：9
            int height = mPreviewView.getHeight();
            int width = mPreviewView.getWidth();
            if (bitmap.getHeight() / (float) bitmap.getWidth() > mPreviewView.getHeight() / (float) mPreviewView.getWidth()) {
                int newHeight = bitmap.getWidth() * height / width;
                bitmap = cropBitmap(bitmap, bitmap.getWidth(), newHeight);
            } else if (bitmap.getHeight() / (float) bitmap.getWidth() < mPreviewView.getHeight() / (float) mPreviewView.getWidth()) {
                int newWith = bitmap.getHeight() * width / height;
                bitmap = cropBitmap(bitmap, newWith, bitmap.getHeight());
            }
            //大小控制和预览一样
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }

        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        Bitmap finalBitmap = bitmap;
        barcodeScanner.process(inputImage)
                .addOnSuccessListener(barcodes -> {
                    if (barcodes == null || barcodes.isEmpty()) {
                        return;
                    }
                    if (!isAnalyze) {
                        return;
                    }
                    isAnalyze = false;
                    for (Barcode barcode : barcodes) {
                        Point[] cornerPoints = barcode.getCornerPoints();
                        StringBuilder cornerPoint = new StringBuilder();
                        cornerPoint.append("[");
                        if (cornerPoints != null) {
                            for (int i = 0; i < cornerPoints.length; i++) {
                                Point point = cornerPoints[i];
                                cornerPoint.append(point.toString());
                                cornerPoint.append(",");
                                if (i == cornerPoints.length - 1) {
                                    cornerPoint.append("]");
                                }
                            }
                        }
                        Log.i("======", "barcode-getDisplayValue:" + barcode.getDisplayValue()
                                + ",barcode-getRawValue:" + barcode.getRawValue()
                                + ",cornerPoints:" + cornerPoint
                                + ",boundingBox:" + barcode.getBoundingBox());
                    }
                    if (onCameraAnalyserCallback != null) {
                        onCameraAnalyserCallback.onSuccess(finalBitmap, barcodes);
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close())
                .addOnFailureListener(e -> Log.e("======", "onFailure---:" + e));
    }
}