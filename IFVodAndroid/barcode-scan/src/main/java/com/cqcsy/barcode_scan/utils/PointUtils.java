package com.cqcsy.barcode_scan.utils;

import android.graphics.Point;

/**
 * 作者：wangjianxiong
 * 创建时间：2022/9/21
 */
public class PointUtils {

    public static float distance(Point pattern1, Point pattern2) {
        return distance(pattern1.x, pattern1.y, pattern2.x, pattern2.y);
    }

    public static float distance(float aX, float aY, float bX, float bY) {
        float xDiff = aX - bX;
        float yDiff = aY - bY;
        return (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }
}
