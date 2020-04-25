package com.openailab.facetrack.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class InfoView extends View {

    private Paint mPaint;
    private String str = "";

    public InfoView(Context context) {
        this(context, null);
    }

    public InfoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public InfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(15F);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                30F, context.getResources().getDisplayMetrics()));
    }

    public void drawInfo(String name) {
        str = name;
        postInvalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(str, 20F, 250F, mPaint);
    }

}
