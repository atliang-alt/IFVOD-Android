package com.cqcsy.barcode_scan.view;

import static android.graphics.drawable.GradientDrawable.RECTANGLE;

import android.animation.Animator;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.cqcsy.barcode_scan.R;
import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.List;

/**
 * @author : maning
 * @date : 2021/1/7
 * @desc : 扫描结果点View展示
 */
public class ScanResultPointView extends FrameLayout {

    private List<Barcode> resultPoint;
    private OnResultPointClickListener onResultPointClickListener;

    private int resultPointColor;
    private int resultPointStrokeColor;
    private int resultPointSize;
    @DrawableRes
    private int resultPointArrow;
    private int resultPointRadiusCorners;
    private int resultPointStrokeWidth;
    private TextView tv_cancel;
    private FrameLayout fl_result_point_root;
    private View fakeStatusBar;
    private int statusBarHeight;
    private ImageView iv_show_result;
    private Bitmap barcodeBitmap;
    private boolean showCancel;

    public void setOnResultPointClickListener(OnResultPointClickListener onResultPointClickListener) {
        this.onResultPointClickListener = onResultPointClickListener;
    }

    public interface OnResultPointClickListener {
        void onPointClick(String result);

        void onCancel();
    }

    public ScanResultPointView(Context context) {
        this(context, null);
    }

    public ScanResultPointView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanResultPointView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ScanResultPointView);
        resultPointColor = array.getColor(R.styleable.ScanResultPointView_pointColor, ContextCompat.getColor(context, R.color.scan_result_point_color));
        resultPointStrokeColor = array.getColor(R.styleable.ScanResultPointView_pointStrokeColor, ContextCompat.getColor(context, R.color.scan_result_point_stroke_color));
        resultPointStrokeWidth = array.getDimensionPixelSize(R.styleable.ScanResultPointView_pointStrokeWidth, dip2px(2f));
        resultPointRadiusCorners = array.getDimensionPixelSize(R.styleable.ScanResultPointView_pointCorners, dip2px(36f));
        resultPointSize = array.getDimensionPixelSize(R.styleable.ScanResultPointView_pointSize, dip2px(36f));
        resultPointArrow = array.getResourceId(R.styleable.ScanResultPointView_pointArrow, R.drawable.mn_icon_scan_default_result_point_arrow);
        showCancel = array.getBoolean(R.styleable.ScanResultPointView_showCancel, true);
        array.recycle();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.scan_result_point_view, this);
        fakeStatusBar = view.findViewById(R.id.fakeStatusBar);
        iv_show_result = view.findViewById(R.id.iv_show_result);
        tv_cancel = view.findViewById(R.id.tv_cancel);
        fl_result_point_root = view.findViewById(R.id.fl_result_point_root);

        if (showCancel) {
            tv_cancel.setVisibility(View.VISIBLE);
        } else {
            tv_cancel.setVisibility(View.GONE);
        }
        tv_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //隐藏View
                if (onResultPointClickListener != null) {
                    onResultPointClickListener.onCancel();
                }
                removeAllPoints();
            }
        });
        iv_show_result.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //拦截点击事件
            }
        });
    }

    public void setData(List<Barcode> results, Bitmap barcode) {
        this.resultPoint = results;
        this.barcodeBitmap = barcode;
        drawableResultPoint();
    }

    public void removeAllPoints() {
        fl_result_point_root.removeAllViews();
    }

    private void drawableResultPoint() {
        iv_show_result.setImageBitmap(barcodeBitmap);
        removeAllPoints();
        if (resultPoint == null || resultPoint.size() == 0) {
            if (onResultPointClickListener != null) {
                onResultPointClickListener.onCancel();
            }
            return;
        }
        if (showCancel) {
            if (resultPoint.size() == 1) {
                tv_cancel.setVisibility(View.INVISIBLE);
            } else {
                tv_cancel.setVisibility(View.VISIBLE);
            }
        }


        for (int j = 0; j < resultPoint.size(); j++) {
            Barcode barcode = resultPoint.get(j);
            Rect boundingBox = barcode.getBoundingBox();
            int centerX = boundingBox.centerX();
            int centerY = boundingBox.centerY();

            View view = LayoutInflater.from(getContext()).inflate(R.layout.scan_result_point_item_view, null);
            ImageView iv_point_bg = view.findViewById(R.id.iv_point_bg);
            ImageView iv_point_arrow = view.findViewById(R.id.iv_point_arrow);
            if (resultPointArrow != -1) {
                iv_point_arrow.setImageResource(resultPointArrow);
            }
            //位置
            view.setX(centerX - resultPointSize / 2.0f);
            view.setY(centerY - resultPointSize / 2.0f);

            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setCornerRadius(resultPointRadiusCorners);
            gradientDrawable.setShape(RECTANGLE);
            gradientDrawable.setStroke(resultPointStrokeWidth, resultPointStrokeColor);
            gradientDrawable.setColor(resultPointColor);

            iv_point_bg.setImageDrawable(gradientDrawable);

            //点的大小
            ViewGroup.LayoutParams lpPoint = iv_point_bg.getLayoutParams();
            lpPoint.width = resultPointSize;
            lpPoint.height = resultPointSize;
            iv_point_bg.setLayoutParams(lpPoint);

            //箭头大小
            if (resultPoint.size() > 1) {
                ViewGroup.LayoutParams lpArrow = iv_point_arrow.getLayoutParams();
                lpArrow.width = resultPointSize / 2;
                lpArrow.height = resultPointSize / 2;
                iv_point_arrow.setLayoutParams(lpArrow);
                iv_point_arrow.setVisibility(View.VISIBLE);
            } else {
                //一个不需要箭头
                iv_point_arrow.setVisibility(View.GONE);
            }

            iv_point_bg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onResultPointClickListener != null) {
                        onResultPointClickListener.onPointClick(barcode.getDisplayValue());
                    }
                }
            });
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl_result_point_root.addView(view, lp);
            startScaleAnim(view);
        }
        int childCount = fl_result_point_root.getChildCount();
        if (childCount <= 0) {
            //关闭页面
            if (onResultPointClickListener != null) {
                onResultPointClickListener.onCancel();
            }
        }
    }

    private void startScaleAnim(View view) {
        Keyframe kf1 = Keyframe.ofFloat(0, 1);
        Keyframe kf2 = Keyframe.ofFloat(0.5f, 0.8f);
        Keyframe kf3 = Keyframe.ofFloat(1, 1);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofKeyframe("scaleX", kf1, kf2, kf3);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofKeyframe("scaleY", kf1, kf2, kf3);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(800);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setRepeatCount(1);
        animator.setStartDelay(2000);
        animator.start();
    }

    public void setStatusBarHeight(int statusBarHeight) {
        this.statusBarHeight = statusBarHeight;
        ViewGroup.LayoutParams fakeStatusBarLayoutParams = fakeStatusBar.getLayoutParams();
        fakeStatusBarLayoutParams.height = statusBarHeight;
        fakeStatusBar.setLayoutParams(fakeStatusBarLayoutParams);
    }

    public int dip2px(float dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}