package com.cqcsy.barcode_scan.callback;

import android.graphics.Bitmap;

import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.List;

/**
 * 扫码结果回调
 */
public interface OnScanResultCallback {
    /**
     * 扫码成功的回调
     *
     * @param bitmap   扫码结果截图
     * @param barcodes 扫码结果集合
     * @return 是否播放声音 振动
     */
    boolean onSuccess(Bitmap bitmap, List<Barcode> barcodes);
}
