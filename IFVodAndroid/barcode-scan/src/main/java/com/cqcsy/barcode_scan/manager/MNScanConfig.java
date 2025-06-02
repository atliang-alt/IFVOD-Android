package com.cqcsy.barcode_scan.manager;


import androidx.annotation.RawRes;

import java.io.Serializable;

/**
 * Created by maning on 2017/12/7.
 * 启动Activity的一些配置参数
 */

public class MNScanConfig implements Serializable {

    private static final long serialVersionUID = -5260676142223049891L;

    //扫描声音
    private boolean showBeep;
    //扫描震动
    private boolean showVibrate;
    //是否支持手势缩放，默认支持
    private boolean isSupportZoom;
    //显示闪光灯
    private boolean showLightController;
    //是否需要全屏扫描，默认全屏扫描
    private boolean isFullScreenScan;
    private boolean isSupportAutoZoom;
    @RawRes
    private int beepSoundRaw = -1;

    private MNScanConfig(Builder builder) {
        showBeep = builder.showBeep;
        showVibrate = builder.showVibrate;
        showLightController = builder.showLightController;
        isFullScreenScan = builder.isFullScreenScan;
        isSupportZoom = builder.isSupportZoom;
        beepSoundRaw = builder.beepSoundRaw;
        isSupportAutoZoom = builder.isSupportAutoZoom;
    }

    public boolean isShowBeep() {
        return showBeep;
    }

    public boolean isShowVibrate() {
        return showVibrate;
    }

    public boolean isShowLightController() {
        return showLightController;
    }

    public boolean isFullScreenScan() {
        return isFullScreenScan;
    }

    public boolean isSupportZoom() {
        return isSupportZoom;
    }

    public boolean isSupportAutoZoom() {
        return isSupportAutoZoom;
    }

    public int getBeepSoundRaw() {
        return beepSoundRaw;
    }

    public static class Builder {
        private boolean showBeep = true;
        private boolean showVibrate = true;
        //闪光灯
        private boolean showLightController = true;
        //是否需要全屏扫描，默认值扫描扫描框中的二维码
        private boolean isFullScreenScan = true;
        //是否支持手势缩放，默认支持
        private boolean isSupportZoom = true;
        private boolean isSupportAutoZoom = true;
        @RawRes
        private int beepSoundRaw = -1;

        public MNScanConfig builder() {
            return new MNScanConfig(this);
        }

        public Builder isShowBeep(boolean showBeep) {
            this.showBeep = showBeep;
            return this;
        }

        public Builder isShowVibrate(boolean showVibrate) {
            this.showVibrate = showVibrate;
            return this;
        }

        public Builder isShowLightController(boolean showLightController) {
            this.showLightController = showLightController;
            return this;
        }

        public Builder setFullScreenScan(boolean fullScreenScan) {
            isFullScreenScan = fullScreenScan;
            return this;
        }

        public Builder setSupportZoom(boolean supportZoom) {
            isSupportZoom = supportZoom;
            return this;
        }

        public Builder setSupportAutoZoom(boolean supportAutoZoom) {
            isSupportAutoZoom = supportAutoZoom;
            return this;
        }

        public Builder setBeepSoundRaw(@RawRes int beepSoundRaw) {
            this.beepSoundRaw = beepSoundRaw;
            return this;
        }

    }

}
