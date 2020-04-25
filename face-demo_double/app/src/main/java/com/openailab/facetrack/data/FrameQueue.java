package com.openailab.facetrack.data;

import org.opencv.core.Mat;

public class FrameQueue {
    private static final String TAG = "FrameQueue";
    
    private static final class  Holder{
        private static final FrameQueue instance=new FrameQueue();
    }

    public static FrameQueue getInstance(){
        return Holder.instance;
    }

    MatInfo rMatInfo=new MatInfo(),gMatInfo=new MatInfo();

    public Mat getrMat() {
        return rMatInfo.mMat;
    }

    public void setrMat(Mat rMat,long time) {
        synchronized (this){
            rMatInfo.setData(rMat,time);
        }
    }

    public Mat getgMat() {
        return gMatInfo.mMat;
    }

    public void setgMat(Mat gMat,long time) {
        synchronized (this){
            gMatInfo.setData(gMat,time);
        }
    }

    public Mat[] getDualMat(){
        synchronized (this){
            MatInfo rMatInfo = new MatInfo();
            MatInfo gMatInfo = new MatInfo();
            if (getMatInfo(rMatInfo,gMatInfo)){
                return  new Mat[]{rMatInfo.mMat,gMatInfo.mMat};
            } else {
                return  null;
            }
        }
    }

    public boolean getMatInfo(MatInfo rinfo, MatInfo ginfo) {
        if (isSameTime()) {
            //todo
            rinfo.time = rMatInfo.time;
//                Log.i(TAG, "getDualYuvInfo:infoCLO "+infoColor.time);
            rinfo.mMat = rMatInfo.mMat;
            rMatInfo.clear();

            ginfo.time = gMatInfo.time;
//                Log.i(TAG, "getDualYuvInfo:infoIR  "+infoFrared.time);
            ginfo.mMat = gMatInfo.mMat;
            gMatInfo.clear();
            return true;
        }
        return false;
    }

    private boolean isSameTime() {
        if (rMatInfo.isNews && gMatInfo.isNews && Math.abs(rMatInfo.time - gMatInfo.time) < 100) {
            return true;
        }
        return false;
    }

    //相机出来的yuv数据对象
    public static class MatInfo {
        public Mat mMat; // 存放的是YUV的源数据
        public long time; //数据更新的时间
        public boolean isNews = false;//数据是否为空

        public void setData(Mat mat, long time) {
            this.time = time;
            this.mMat = mat;
            isNews = true;
        }

        public void clear() {
            isNews = false;
        }
    }

}
