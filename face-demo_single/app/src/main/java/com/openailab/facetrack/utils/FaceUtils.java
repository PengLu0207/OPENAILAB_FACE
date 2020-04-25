package com.openailab.facetrack.utils;

import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

import com.bumptech.glide.util.LogTime;
import com.openailab.facelibrary.FaceAPP;
import com.openailab.facelibrary.FaceAPP.Image;
import com.openailab.facelibrary.FaceInfo;
import com.openailab.facetrack.widget.InfoView;
import com.openailab.facetrack.widget.RectView;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Objects;

public class FaceUtils {
    private static final String TAG = "FaceUtils";

    private int matW = 1620;
    private int matH = 1920;

    private Mat srcMatR = new Mat(matW, matH, CvType.CV_8UC1);
    private Mat matR = new Mat(CameraUtils.DEFAULT_WIDTH, CameraUtils.DEFAULT_HEIGHT, CvType.CV_8UC3);

    private FaceAPP mFace = FaceAPP.getInstance();

    private int[] result = new int[1];
    private int[] mDetectRest = new int[1];

    private ArrayList<FaceInfo> mFaceInfos = new ArrayList<>();
    private FrameRateMeter mDetectFrame = new FrameRateMeter();
    private float[] mFeature = new float[512];
    private float[] mFaceScore = new float[1];

    private float ScalX=0f;
    private float ScalY=0f;

    private boolean regIng;

    private static volatile FaceUtils instance;

    private FaceUtils() {
    }

    public static FaceUtils getInstance() {
        synchronized (FaceUtils.class) {
            if (instance == null) {
                instance = new FaceUtils();
            }
        }
        return instance;
    }

    public void openRGB(Camera camera) {
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                // 预览帧数据 YUV420_NV21
                srcMatR.put(0, 0, data);
                camera.addCallbackBuffer(data);
            }
        });
    }

    public void livess(RectView rectView, InfoView infoView, int width, int height) {
        if (ScalX==0f||ScalY==0f){
            ScalX=(float) width/CameraUtils.DEFAULT_HEIGHT;
            Log.e(TAG, "livess: width "+width+"--CameraWIDTH"+CameraUtils.DEFAULT_WIDTH );
            ScalY=(float) height/CameraUtils.DEFAULT_WIDTH;
            Log.e(TAG, "livess: height "+height+"--CameraHEIGHT"+CameraUtils.DEFAULT_HEIGHT );
        }

        mDetectFrame.drawFrameCount();
        if (!srcMatR.empty() && !regIng) {
            Imgproc.cvtColor(srcMatR, matR, Imgproc.COLOR_YUV2RGB_NV21, 4);
          //  Core.transpose(matR,matR);
            Core.rotate(matR,matR,Core.ROTATE_90_COUNTERCLOCKWISE);
            Image image = FaceAPP.getInstance().new Image();
            image.matAddrframe = matR.getNativeObjAddr();

            int res = mFace.Detect(image, mFaceInfos, mDetectRest);
            if (res == mFace.SUCCESS) {
                FaceInfo faceInfo = mFaceInfos.get(0);
                drawFrame(rectView, faceInfo, width, height, mDetectFrame.getFps());
                mFace.GetFeature(image, faceInfo, mFeature, result);
                String name = mFace.QueryDB(mFeature, mFaceScore);
                float score = mFaceScore[0];
                infoView.drawInfo(score > 0.69 && !Objects.equals(name, "unknown") ? "姓名：" + name + "相似度：" + score : "未注册");
                mFaceInfos.clear();
            } else {
                infoView.drawInfo("未检测到人脸");
                rectView.clearRect();
            }

        } else {
            infoView.drawInfo("未检测到人脸");
            rectView.clearRect();
        }
    }


    public void drawFrame(RectView rectView, FaceInfo faceInfo, int width, int height, float fps) {
        Log.d(TAG, "drawFrame: faceInfo"+ faceInfo.mRect.left +" "+faceInfo.mRect.top+" "+faceInfo.mRect.right+" "+faceInfo.mRect.bottom);
        
        rectView.drawFaceRect(new Rect(
                (int) (faceInfo.mRect.left * ScalX),
                (int)(faceInfo.mRect.top * ScalY),
                (int)(faceInfo.mRect.right * ScalX),
                (int)(faceInfo.mRect.bottom * ScalY)
        ), fps);

    }

    public int register(String name) {
        regIng = true;
        Imgproc.cvtColor(srcMatR, matR, Imgproc.COLOR_YUV2RGBA_NV12, 4);
        Core.rotate(matR,matR,Core.ROTATE_90_COUNTERCLOCKWISE);
        Image image = FaceAPP.getInstance().new Image();
        Mat frame = matR.clone();
        image.matAddrframe = frame.getNativeObjAddr();
        int feature = mFace.GetFeature(image, mFeature, new ArrayList<FaceInfo>(), result);
        mFace.QueryDB(mFeature, mFaceScore);
        if (feature == 0 && mFaceScore[0] < 0.6) {
            regIng = false;
            int addDB = mFace.AddDB(mFeature, name);
            mFace.SaveDB();
            return addDB;
        }
        regIng = false;
        return -1;
    }
}
