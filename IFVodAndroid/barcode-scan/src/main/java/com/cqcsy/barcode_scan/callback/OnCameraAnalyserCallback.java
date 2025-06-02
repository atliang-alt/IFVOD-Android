package com.cqcsy.barcode_scan.callback;

import android.graphics.Bitmap;

import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.List;

public interface OnCameraAnalyserCallback {
    void onSuccess(Bitmap bitmap, List<Barcode> barcodes);
}
