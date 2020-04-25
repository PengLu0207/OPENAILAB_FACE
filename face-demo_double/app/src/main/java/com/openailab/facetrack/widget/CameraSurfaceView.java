package com.openailab.facetrack.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.openailab.facetrack.R;
import com.openailab.facetrack.utils.CameraUtils;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraSurfaceView";

    private SurfaceHolder mSurfaceHolder;
    int cameraID;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.mSurfaceView);
        cameraID = typedArray.getInt(R.styleable.mSurfaceView_cameraType,0);
//                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CameraSurfaceView);
        if(cameraID==0){
            this.setZOrderOnTop(true); //设置置顶
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        CameraUtils.openCamera(CameraUtils.DESIRED_PREVIEW_FPS,cameraID);
        Log.e(TAG, "surfaceCreated: cameraID"+cameraID);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        Log.d(TAG, "surfaceChanged: format "+format +" width"+width+"  height"+height);
        CameraUtils.startPreviewDisplay(holder,cameraID);
        Log.e(TAG, "surfaceChanged: startPreviewDisplay "+ cameraID );
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraUtils.releaseCamera(cameraID);
        Log.e(TAG, "surfaceDestroyed: releaseCamera "+cameraID);
    }

}
