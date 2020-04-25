package com.openailab.facetrack.utils;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraUtils {
    private static final String TAG = "CameraUtils";

    public static final int DEFAULT_WIDTH = 1920;
    public static final int DEFAULT_HEIGHT = 1080;

//    public static final int DEFAULT_WIDTH = 640;
//    public static final int DEFAULT_HEIGHT = 480;

    public static final int DESIRED_PREVIEW_FPS = 30;

    private static CameraInfo backCameraInfo=new CameraInfo(),frontCameraInfo=new CameraInfo();
    private static int mCameraPreviewFps;
    private static int mOrientation = 0;

    /**
     * 根据ID打开相机
     * @param expectFps
     * @param cameraID
     */
    public static void openCamera(int expectFps,int cameraID) {

        Log.d(TAG, "NumberOfCameras: "+Camera.getNumberOfCameras());

        if  (cameraID==0){
            if ( backCameraInfo.mCamera != null) {
                throw new RuntimeException("camera already initialized!");
            }
            Camera camera = Camera.open( Camera.CameraInfo.CAMERA_FACING_BACK);
            // 没有摄像头时，抛出异常
            if (camera == null) {
                throw new RuntimeException("Unable to open cameraID"+cameraID);
            }

            backCameraInfo.setCameraID(cameraID);
            backCameraInfo.setCamera(camera);

            FaceUtils.getInstance().openRGB(backCameraInfo.mCamera);

            Camera.Parameters parameters = camera.getParameters();
//            mCameraPreviewFps = CameraUtils.chooseFixedPreviewFps(parameters, expectFps * 1000);
//            parameters.setRecordingHint(true);
            camera.setParameters(parameters);
            setPreviewSize(camera, CameraUtils.DEFAULT_WIDTH, CameraUtils.DEFAULT_HEIGHT);
        }
        else if (cameraID==1){
            if ( frontCameraInfo.mCamera != null) {
                throw new RuntimeException("camera already initialized!");
            }

            Camera camera=Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            if (camera == null) {
                throw new RuntimeException("Unable to open cameraID"+cameraID);
            }

            frontCameraInfo.setCameraID(cameraID);
            frontCameraInfo.setCamera(camera);

            FaceUtils.getInstance().openGray(frontCameraInfo.mCamera);

            Camera.Parameters parameters = camera.getParameters();
//            mCameraPreviewFps = CameraUtils.chooseFixedPreviewFps(parameters, expectFps * 1000);
//            parameters.setRecordingHint(true);
            camera.setParameters(parameters);
            setPreviewSize(camera, CameraUtils.DEFAULT_WIDTH, CameraUtils.DEFAULT_HEIGHT);
        }else {
            throw new IllegalArgumentException("not support cameraID"+cameraID);
        }
    }

    /**
     * 开始预览
     *
     * @param holder
     */
    public static void startPreviewDisplay(SurfaceHolder holder,int cameraID) {
        Camera mCamera;
        if (cameraID==0){
            mCamera=backCameraInfo.mCamera;
        }else if (cameraID==1){
            mCamera=frontCameraInfo.mCamera;
        }else {
            throw new IllegalArgumentException("cameraID not support"+cameraID);
        }

        if (mCamera == null) {
            throw new IllegalStateException("Camera must be set when start preview");
        }
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(270);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 释放相机
     */
    public static void releaseCamera(int cameraID) {
        Camera mCamera;
        if (cameraID==0){
            mCamera=backCameraInfo.mCamera;
        }else if (cameraID==1){
            mCamera=frontCameraInfo.mCamera;
        }else {
            throw new IllegalArgumentException("cameraID not support"+cameraID);
        }

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 开始预览
     */
    public static void startPreview() {
        if (backCameraInfo.mCamera != null) {
            backCameraInfo.mCamera.startPreview();
        }

        if (frontCameraInfo.mCamera!=null){
            frontCameraInfo.mCamera.startPreview();
        }
    }

    /**
     * 停止预览
     */
    public static void stopPreview() {
        if (backCameraInfo.mCamera != null) {
            backCameraInfo.mCamera.stopPreview();
        }
        if (frontCameraInfo.mCamera!=null){
            frontCameraInfo.mCamera.stopPreview();
        }
    }


    /**
     * 设置预览大小
     *
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    public static void setPreviewSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPreviewSizes(),
                expectWidth, expectHeight);
        Log.d(TAG, "setPreviewSize:  width+"+size.width+" height+"+size.height);
        parameters.setPreviewSize(size.width, size.height);
        camera.setParameters(parameters);
    }

    /**
     * 获取预览大小
     *
     * @return
     */
//    public static Camera.Size getPreviewSize() {
//        if (mCamera != null) {
//            return mCamera.getParameters().getPreviewSize();
//        }
//        return null;
//    }


    /**
     * 计算最完美的Size
     *
     * @param sizes
     * @param expectWidth
     * @param expectHeight
     * @return
     */
    public static Camera.Size calculatePerfectSize(List<Camera.Size> sizes, int expectWidth,
                                                   int expectHeight) {
        // 根据宽度进行排序
        sortList(sizes);
        Camera.Size result = sizes.get(0);
        // 判断存在宽或高相等的Size
        boolean widthOrHeight = false;
        // 辗转计算宽高最接近的值
        for (Camera.Size size : sizes) {
            Log.d(TAG, "sizes list: "+ size.width+" "+size.height);
            // 如果宽高相等，则直接返回
            if (size.width == expectWidth && size.height == expectHeight) {
                result = size;
                break;
            }
            // 仅仅是宽度相等，计算高度最接近的size
            if (size.width == expectWidth) {
                widthOrHeight = true;
                if (Math.abs(result.height - expectHeight)
                        > Math.abs(size.height - expectHeight)) {
                    result = size;
                }
            }
            // 高度相等，则计算宽度最接近的Size
            else if (size.height == expectHeight) {
                widthOrHeight = true;
                if (Math.abs(result.width - expectWidth)
                        > Math.abs(size.width - expectWidth)) {
                    result = size;
                }
            }
            // 如果之前的查找不存在宽或高相等的情况，则计算宽度和高度都最接近的期望值的Size
            else if (!widthOrHeight) {
                if (Math.abs(result.width - expectWidth)
                        > Math.abs(size.width - expectWidth)
                        && Math.abs(result.height - expectHeight)
                        > Math.abs(size.height - expectHeight)) {
                    result = size;
                }
            }
        }
        return result;
    }

    /**
     * 排序
     *
     * @param list
     */
    private static void sortList(List<Camera.Size> list) {
        Collections.sort(list, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size pre, Camera.Size after) {
                if (pre.width > after.width) {
                    return 1;
                } else if (pre.width < after.width) {
                    return -1;
                }
                return 0;
            }
        });
    }

    /**
     * 选择合适的FPS
     *
     * @param parameters
     * @param expectedThoudandFps 期望的FPS
     * @return
     */
    public static int chooseFixedPreviewFps(Camera.Parameters parameters, int expectedThoudandFps) {
        List<int[]> supportedFps = parameters.getSupportedPreviewFpsRange();
        for (int[] entry : supportedFps) {
            if (entry[0] == entry[1] && entry[0] == expectedThoudandFps) {
                parameters.setPreviewFpsRange(entry[0], entry[1]);
                return entry[0];
            }
        }
        int[] temp = new int[2];
        int guess;
        parameters.getPreviewFpsRange(temp);
        if (temp[0] == temp[1]) {
            guess = temp[0];
        } else {
            guess = temp[1] / 2;
        }
        return guess;
    }

    /**
     * 设置预览角度，setDisplayOrientation本身只能改变预览的角度
     * previewFrameCallback以及拍摄出来的照片是不会发生改变的，拍摄出来的照片角度依旧不正常的
     * 拍摄的照片需要自行处理
     * 这里Nexus5X的相机简直没法吐槽，后置摄像头倒置了，切换摄像头之后就出现问题了。
     *
     * @param activity
     */
//    public static int calculateCameraPreviewOrientation(Activity activity) {
//        Camera.CameraInfo info = new Camera.CameraInfo();
//        Camera.getCameraInfo(mcameraID, info);
//        int rotation = activity.getWindowManager().getDefaultDisplay()
//                .getRotation();
//        int degrees = 0;
//        switch (rotation) {
//            case Surface.ROTATION_0:
//                degrees = 0;
//                break;
//            case Surface.ROTATION_90:
//                degrees = 90;
//                break;
//            case Surface.ROTATION_180:
//                degrees = 180;
//                break;
//            case Surface.ROTATION_270:
//                degrees = 270;
//                break;
//            default:
//                break;
//        }
//
//        int result;
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            result = (info.orientation + degrees) % 360;
//            result = (360 - result) % 360;
//        } else {
//            result = (info.orientation - degrees + 360) % 360;
//        }
//        mOrientation = result;
//        return result;
//    }


    /**
     * 获取当前的Camera ID
     *
     * @return
     */
//    public static int getCameraID() {
//        return mCameraID;
//    }

    /**
     * 获取当前预览的角度
     *
     * @return
     */
    public static int getPreviewOrientation() {
        return mOrientation;
    }

    /**
     * 获取FPS（千秒值）
     *
     * @return
     */
    public static int getCameraPreviewThousandFps() {
        return mCameraPreviewFps;
    }

    public static class CameraInfo{
        private int CameraID;
        private Camera mCamera;

        public CameraInfo() {
        }

        public CameraInfo(int cameraID, Camera camera) {
            CameraID = cameraID;
            mCamera = camera;
        }

        public int getCameraID() {
            return CameraID;
        }

        public void setCameraID(int cameraID) {
            CameraID = cameraID;
        }

        public Camera getCamera() {
            return mCamera;
        }

        public void setCamera(Camera camera) {
            mCamera = camera;
        }
    }



}
