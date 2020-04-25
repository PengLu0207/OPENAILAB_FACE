package com.openailab.facetrack.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class RectView extends View {
    private static final String TAG = "RectView";

    private Rect rect;
    private Paint mPaint;
    private Paint mTpaint;

    private String str = "";

    public RectView(Context context) {
        this(context, null);
    }

    public RectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
        initTPaint(context);

    }

    private void initTPaint(Context context) {
        mTpaint = new Paint();
        mTpaint.setAntiAlias(true);
        mTpaint.setColor(Color.GREEN);
        mTpaint.setStrokeWidth(15F);
        mTpaint.setTextAlign(Paint.Align.LEFT);
        mTpaint.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                150F, context.getResources().getDisplayMetrics()));

    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(6F);
        mPaint.setColor(Color.GREEN);
    }

    public void drawFaceRect(Rect rect, Float fps) {
        this.rect = rect;
        str = "FPS:" + String.format("%.2f", fps);
        postInvalidate();
    }

    public void clearRect() {
        rect = new Rect(-40, -40, -40, -40);
        str = "FPS:0";
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (rect != null)
        //region draw line
        {
//            Log.d(TAG, "onDraw: "+rect.left+" "+rect.top+" "+rect.right+" "+rect.bottom);
            /**
             * 左上角的竖线
             */
            canvas.drawLine(
                    rect.left,
                    rect.top,
                    rect.left,
                    (rect.top + 26),
                    mPaint
            );
            /**
             * 左上角的横线
             */
            canvas.drawLine(
                    rect.left,
                    rect.top,
                    (rect.left + 26),
                    rect.top,
                    mPaint
            );

            /**
             * 右上角的竖线
             */
            canvas.drawLine(
                    rect.right,
                    rect.top,
                    (rect.right - 26),
                    rect.top,
                    mPaint
            );
            /**
             * 右上角的横线
             */
            canvas.drawLine(
                    rect.right,
                    rect.top,
                    rect.right,
                    (rect.top + 26),
                    mPaint
            );
            /**
             * 左下角的竖线
             */
            canvas.drawLine(
                    rect.left,
                    rect.bottom,
                    rect.left,
                    (rect.bottom - 26),
                    mPaint
            );
            /**
             * 左下角的横线
             */
            canvas.drawLine(
                    rect.left,
                    rect.bottom,
                    (rect.left + 26),
                    rect.bottom,
                    mPaint
            );

            /**
             * 右下角的竖线
             */
            canvas.drawLine(
                    rect.right,
                    rect.bottom,
                    rect.right,
                    (rect.bottom - 26),
                    mPaint
            );
            /**
             * 右下角的横线
             */
            canvas.drawLine(
                    rect.right,
                    rect.bottom,
                    (rect.right - 26),
                    rect.bottom,
                    mPaint
            );
        }
        //endregion

        canvas.drawText(str, 20F, 200F, mTpaint);
    }
}
