/*License Agreement
 For Open Source Computer Vision Library
 (3-clause BSD License)


 Copyright (C) 2000-2018, Intel Corporation, all rights reserved.
 Copyright (C) 2009-2011, Willow Garage Inc., all rights reserved.
 Copyright (C) 2009-2016, NVIDIA Corporation, all rights reserved.
 Copyright (C) 2010-2013, Advanced Micro Devices, Inc., all rights reserved.
 Copyright (C) 2015-2016, OpenCV Foundation, all rights reserved.
 Copyright (C) 2015-2016, Itseez Inc., all rights reserved.
 Third party copyrights are property of their respective owners.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
•Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
•Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
•Neither the names of the copyright holders nor the names of the contributors may be used to endorse or promote products derived from this software without specific prior written permission.

This software is provided by the copyright holders and contributors "as is" and any express or implied warranties, including, but not limited to, the implied warranties of merchantability and fitness for
a particular purpose are disclaimed. In no event shall copyright holders or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages (including, but not limited to,
procurement of substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise)
arising in any way out of the use of this software, even if advised of the possibility of such damage
*/
package com.openailab.sdkdemo;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.openailab.facelibrary.FaceAPP;
import com.openailab.facelibrary.FaceInfo;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends Activity {

    // Used to load the 'native-lib' library on application startup.
    private static final String TAG = "Opencv::Activity";
    private Mat mFrontFrame;
    private Mat mRgbaFrame;

    private int mWidth;
    private int mHeight;
    Thread mainLoop = null;
    private Lock lock = null;
    private myDrawRectView drawRectView;
    private SurfaceView frontView, backView;
    private VideoUtil videoUtilFront, videoUtilBack;
    public final int camWidth = 640;
    public final int camHeight = 480;
    public final int matWidth = 720;
    public final int matHeight = 640;
    private final static int MAX_REGISTER = FileOperator.MAX_REGISTER;
    private TextView tv_time, calibrateNum;
    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private boolean hasCameraPermission;
    private boolean hasExtSDPermission;
    private final int CALIBRATE = 1;
    private final int SHOWTOAST = 4;
    private FaceAPP face = FaceAPP.GetInstance();//FaceAPP.getInstance();
    private int fIndex = 0;
    private float[][] flist = new float[50][128];
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private boolean noExit = true;
    private final MyHandler mHandler = new MyHandler(this);

    FaceAPP.Image image = FaceAPP.GetInstance().new Image();
    FaceAPP.Image frontimage = FaceAPP.GetInstance().new Image();

    private String editTextString, showName;
    private List<FaceInfo> FaceInfos;

    private final static int FACENUM = 1;//1为单框显示，3为3框显示
    private final static int LIVENESS = 0;//0为非活体检测，1为活体检测
    public byte[] tmpPos = new byte[1024 * FACENUM];

    String[] faceparams = {"a", "b", "c", "d", "factor", "min_size", "clarity", "perfoptimize", "livenessdetect", "gray2colorScale", "frame_num", "quality_thresh", "mode", "facenum"};
    double[] VALUE = {0.75, 0.8, 0.9, 0.6, 0.65, 40, 200, 0, LIVENESS, 0.5, 1, 0.8, 1, FACENUM};

    private final static String CALIBRATE_PATH = "/sdcard/openailab/calibrate.conf";
    private int CALIBRATION = 10;
    private int calibrate_num = CALIBRATION;
    float[] calibrate_scale = {0};
    int [] calibrate_rect = {0,0,0,0};
    private Button calibrateBtn;
    private RelativeLayout calibrateLayout;
    private boolean isNeedToFlipBack = false;  //qc true
    private boolean isNeedToFlipFront = false;  //qc true

    private final static long MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;
    private boolean canCloseCam = false;
    private boolean isDialogShow=false;
    public float[] feature = new float[128];


    /**
     * 声明一个静态的Handler内部类，并持有外部类的弱引用
     */
    private class MyHandler extends Handler {

        private final WeakReference<MainActivity> mActivty;

        private MyHandler(MainActivity mActivty) {
            this.mActivty = new WeakReference<MainActivity>(mActivty);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //Log.d("zheng", "handleMessage:" + msg.what);
            switch (msg.what) {
                case CALIBRATE: {
                    if (msg.arg1 == 1) {
                        calibrateNum.setText(calibrate_num + "");
                        if (calibrate_num == 0) {
                            Log.d("zheng", "calibrate scale : " + calibrate_scale[0] + " rect : " + calibrate_rect[0] + " " + calibrate_rect[1] + " " + calibrate_rect[2] + " " + calibrate_rect[3]);
                            FileOperator.saveCalibrate(CALIBRATE_PATH, calibrate_rect, calibrate_scale[0]);
                            calibrateLayout.setVisibility(View.GONE);
                            calibrateNum.setText(CALIBRATION + "");
                            isDialogShow=false;
                        }
                    }

                }
                break;
                case SHOWTOAST: {
                    //    Log.d("zheng", "注册");
                    if (msg.arg1 == 1) {
                        tv_time.setText("注册成功");
                    } else if (msg.arg1 == 0) {
                        tv_time.setText("你是:" + showName);
                        Log.d("zheng", "toast:" + showName);
                        showName = "点击人脸注册";
                    } else if (msg.arg1 == 2) {
                        tv_time.setText("已经注册超过" + MAX_REGISTER + "人");
                    } else if (msg.arg1 == 3) {
                        tv_time.setText("点击图像人脸注册");
                    } else if (msg.arg1 == 5) {
                        tv_time.setText("注册请输入名称");
                    }else if (msg.arg1 == 6) {
                        tv_time.setText(msg.obj+"已注册过了");
                    }
                    break;
                }
            }
        }
    }


    boolean drawRect(List<com.openailab.facelibrary.FaceInfo> faceInfos,Mat mat){
        //  Log.d("zheng","drawRectView????");
        for (int icount = 0; icount < faceInfos.size(); icount++) {
            FaceInfo info = faceInfos.get(0);
            drawRectView.updateRect((info.mRect.left+ calibrate_rect[0]) * drawRectView.getWidth() / camWidth, (info.mRect.top+calibrate_rect[1]) * drawRectView.getHeight() / camHeight, (info.mRect.right+ calibrate_rect[0]) * drawRectView.getWidth() / camWidth, (info.mRect.bottom+calibrate_rect[1]) * drawRectView.getHeight() / camHeight);
         //   drawRectView.updateRect((info.mRect.left) * drawRectView.getWidth() / camWidth, (info.mRect.top) * drawRectView.getHeight() / camHeight, (info.mRect.right) * drawRectView.getWidth() / camWidth, (info.mRect.bottom) * drawRectView.getHeight() / camHeight);

        }
        //   Log.d("zheng","faceInfos.size():"+faceInfos.size());
        if (faceInfos.size() == 0)
            drawRectView.updateRect(0, 0, 0, 0);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //copyFilesFassets(this,"openailab","/mnt/sdcard/openailab");
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        getWindow().setAttributes(params);


//        // 错误捕获 必须提前绑定
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        drawRectView = (myDrawRectView) findViewById(R.id.mipi_preview_content);
        drawRectView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        drawRectView.setZOrderOnTop(true);
        tv_time = (TextView) findViewById(R.id.tv_time);
        calibrateBtn = (Button) findViewById(R.id.calibrateBtn);
        calibrateNum = (TextView) findViewById(R.id.calibrateNum);
        calibrateLayout = (RelativeLayout) findViewById(R.id.calibrateLayout);

        lock = new ReentrantLock();

        FaceInfos = new ArrayList<FaceInfo>();
        copyFilesFassets(this, "openailab", "/mnt/sdcard/openailab");


        sharedPref = getSharedPreferences(getString(R.string.pref_start_id), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        face.SetParameter(faceparams, VALUE);

        frontView = (SurfaceView) findViewById(R.id.frontView);
        backView = (SurfaceView) findViewById(R.id.backView);
        videoUtilFront = new VideoUtil(frontView.getHolder(), matWidth, matHeight, this, Camera.CameraInfo.CAMERA_FACING_FRONT);
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();//屏幕分辨率容器
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        mWidth = mDisplayMetrics.widthPixels;
        mHeight = 650;
        videoUtilBack = new VideoUtil(backView.getHolder(), matWidth, matHeight, this, Camera.CameraInfo.CAMERA_FACING_BACK);

        mFrontFrame = new Mat(480, 640, CvType.CV_8UC1);
        //   mBackFrame = new Mat(480, 640, CvType.CV_8UC1);

        backView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isDialogShow){
                    return;
                }else{
                    isDialogShow=true;
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                    lastClickTime = currentTime;
                    //    drawMat = false;
                    //switch to sleep mode
                    mixController.setState(mixController.STATE_FACE_RECOGNIZE, mixController.STATE_FACE_REGISTER);
                    // Toast.makeText(MainActivity.this,"注册成功",Toast.LENGTH_LONG).show();
                    final View pwdEntryView = MainActivity.this.getLayoutInflater().inflate(
                            R.layout.dialog_exit_pwd, null);

                    //图片导入
                    Button file_browser = (Button) pwdEntryView.findViewById(R.id.file_browser);
                    file_browser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent openFileBrowser = new Intent(MainActivity.this, GetSDTreeActivity.class);
                            videoUtilFront.stopPreview();
                            videoUtilBack.stopPreview();
                            startActivity(openFileBrowser);
                            //    MainActivity.this.finish();
                        }
                    });

                    final EditText register_edittext = (EditText) pwdEntryView.findViewById(R.id.register_edittext);
                    register_edittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});//10 char

                    TextView sdk_version_text = (TextView) pwdEntryView.findViewById(R.id.sdk_version_text);
                    sdk_version_text.setText("SDK Version: " + face.GetVersion() + "    " + "Lib Version: " + face.GetFacelibVersion());


                    new AlertDialog.Builder(MainActivity.this).setTitle("确认注册吗？")
                            .setIcon(android.R.drawable.ic_input_add)
                            .setView(pwdEntryView)
                            .setCancelable(false)
                            .setPositiveButton("点此采集图片注册", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击“确认”后的采集图片，用于注册
                                    //   drawMat = false;// 默认关闭true;
                                    isDialogShow=false;
                                    editTextString = register_edittext.getText().toString();
                                    editTextString = editTextString.replaceAll(" ", "");
                                    editTextString = editTextString.replaceAll("\r", "");
                                    editTextString = editTextString.replaceAll("\n", "");


                                    if (editTextString == null || "".equals(editTextString)) {
                                        Message tempMsg = mHandler.obtainMessage();
                                        tempMsg.arg1 = 5;
                                        tempMsg.what = SHOWTOAST;
                                        mHandler.sendMessage(tempMsg);

                                        //        drawMat = false;//true;
                                        mixController.setState(mixController.STATE_IDLE, mixController.STATE_FACE_RECOGNIZE);
                                        return;
                                    }
                                    mixController.setState(mixController.STATE_FACE_REGISTER, mixController.STATE_FACE_RECOGNIZE);
                                    Log.d("morrisdebug", "name is " + register_edittext.getText().toString());


                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //   drawMat = false;//true;
                                    mixController.setState(mixController.STATE_IDLE, mixController.STATE_FACE_RECOGNIZE);
                                    isDialogShow=false;
                                    // 点击“返回”后的操作,这里不设置没有任何操作
                                    //Toast.makeText(MainActivity.this, "你点击了返回键", Toast.LENGTH_LONG).show();
                                }
                            }).show();
                }
            }
        });


        if (!setCalibrateFromFile()) {
            gotoCalibrate();
        }
        face.OpenDB();
        //    mixController.setState(mixController.STATE_CALIBRATE, mixController.STATE_IDLE);
        mainLoop = new Thread() {
            public void run() {
                int j;
                int[] i = new int[1];
                float[] high = new float[1];
                long now;
                long pas = 0;

                while (true) {
                    now = System.currentTimeMillis();
                    float fps = 1000 / ((float) (now - pas));
                    pas = System.currentTimeMillis();
                    drawRectView.updateDrawFlag(false, null, fps);

                    if (!noExit) {
                        canCloseCam = true;
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("zheng", "sleeping");
                        continue;
                    }
                    if (!videoUtilFront.isSyncFlag()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("zheng", "syncFlag");
                        continue;
                    }
                    lock.lock();
                    try {
                        mRgbaFrame = videoUtilBack.getmRgba().clone();
                        synchronized (this) {
                            Imgproc.cvtColor(videoUtilFront.getmRgba(), mFrontFrame, Imgproc.COLOR_YUV2RGBA_NV12, 4);
                            Imgproc.cvtColor(videoUtilBack.getmRgba(), mRgbaFrame, Imgproc.COLOR_YUV2RGBA_NV12, 4);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    } finally {
                        lock.unlock();
                    }
                    if (isNeedToFlipBack) {
                        Core.flip(mRgbaFrame, mRgbaFrame, 1);
                    }
                    if (isNeedToFlipFront) {
                        Core.flip(mFrontFrame, mFrontFrame, 1);
                    }

                    switch (mixController.curState) {
                        case mixController.STATE_FACE_RECOGNIZE:

                            break;
                        case mixController.STATE_FACE_REGISTER:
                            Log.d("cpdebugregister", "register");

                            if (editTextString == null || "".equals(editTextString)) {
                                break;
                            }
                            image.matAddrframe = mRgbaFrame.getNativeObjAddr();
                           // j = face.GetFeature(image, feature, i);
                            j=face.GetFeature(image,feature,i);
                            if (j == face.SUCCESS) {
                                Log.d("zheng", "register editTextString:" + editTextString);
//                                String featureStr="";
//                                for (int k = 0 ;k<feature.length;k++){
//                                    featureStr=featureStr+feature[k]+"  ";
//                                }
//                                Log.d("zheng","featureStr:"+featureStr);

                                Message tempMsg = mHandler.obtainMessage();
                                high = new float[1];
                                String name = face.QueryDB(feature,high);
                                Log.i("zheng queryDB",name + " " + high[0]);
                                if(!name.equals("unknown") && high[0] > 0.6){
                                    tempMsg.what =SHOWTOAST ;
                                    tempMsg.arg1 = 6;
                                    tempMsg.obj=name;
                                    mHandler.sendMessage(tempMsg);
                                    mixController.setState(mixController.STATE_IDLE, mixController.STATE_FACE_REGISTER);
                                    break;
                                }


                                int res = face.AddDB(feature, editTextString);
                                Log.d("zheng", "register res:" + res);
                                if (res == FaceAPP.SUCCESS) {
                                    tempMsg.arg1 = 1;
                                } else {
                                    tempMsg.arg1 = 2;
                                }
                                tempMsg.what = SHOWTOAST;
                                mHandler.sendMessage(tempMsg);

                                mixController.setState(mixController.STATE_IDLE, mixController.STATE_FACE_REGISTER);

                            } else if (j == face.ERROR_FAILURE) {
                                mixController.setState(mixController.STATE_IDLE, mixController.STATE_FACE_REGISTER);
                            }
                            editTextString = "";

                            break;


                        case mixController.STATE_IDLE:

                            if (mRgbaFrame.empty() || mRgbaFrame.channels() != 4) {
                                Log.d("morrisdebug", "empty channel check you ");
                                break;
                            }

                            Imgproc.resize(mFrontFrame,mFrontFrame,new Size((int)(mFrontFrame.width() * calibrate_scale[0]),(int)(mFrontFrame.height() * calibrate_scale[0])));
                            frontimage.matAddrframe = mFrontFrame.getNativeObjAddr();
                            //  Imgcodecs.imwrite("/sdcard/openailab/2.jpg", mFrontFrame);

                            Mat tmpMat = new Mat(mRgbaFrame, new Rect(calibrate_rect[0],calibrate_rect[1],calibrate_rect[2]-calibrate_rect[0],calibrate_rect[3]-calibrate_rect[1]));
                            image.matAddrframe = tmpMat.getNativeObjAddr();
                            j= face.GetFeature(image, frontimage, feature, FaceInfos, i);
                            drawRect(FaceInfos,mRgbaFrame);
                            if (j == face.SUCCESS) {
                                float[] score = {0};
                                showName = face.QueryDB(feature, score);
                                Log.i("zheng", "name " + showName + " score " + score[0]);
                                Message tempMsg = mHandler.obtainMessage();
                                tempMsg.what = SHOWTOAST;
                                if (score[0] > VALUE[3]) {
                                    tempMsg.arg1 = 0;
                                } else {
                                    tempMsg.arg1 = 3;
                                }
                                tempMsg.what = SHOWTOAST;
                                mHandler.sendMessage(tempMsg);
                            } else if (j == face.ERROR_FAILURE) {
                                Log.d("zheng", "ERROR_FAILURE");
                                    if (i[0] == face.ERROR_NOT_EXIST) {
                                        drawRect(FaceInfos, mRgbaFrame);
                                    } else if (i[0] == face.ERROR_INVALID_PARAM) {
                                        drawRectView.updateRect(0, 0, 0, 0);
                                    }
                                    Message tempMsg = mHandler.obtainMessage();
                                    tempMsg.arg1 = 3;
                                    tempMsg.what = SHOWTOAST;
                                    mHandler.sendMessage(tempMsg);
                                    Log.d("zheng", "cant recoginze you");
                                }

                            break;
                        case mixController.STATE_CALIBRATE:
                            if (mRgbaFrame.empty() || mRgbaFrame.channels() != 4) {
                                Log.d("zheng", "empty channel check you ");
                                break;
                            }
                            Log.d("zheng", "STATE_CALIBRATE start  ok!");

                            frontimage.matAddrframe = mFrontFrame.getNativeObjAddr();
                            image.matAddrframe = mRgbaFrame.getNativeObjAddr();
                            //    Log.d("zheng", "STATE_CALIBRATE start  ok111111111" + frontimage.matAddrframe);
                            float[] scale = {0};
                            int [] rect = {0,0,0,0};
                            //    Log.d("zheng", "STATE_CALIBRATE start  ok12222222222" + image.matAddrframe);
                            face.Calibration(image,frontimage,scale,rect,i);
                            Log.d("zheng", "scale : " + scale[0] + " rect : " + rect[0] + " " + rect[1] + " " + rect[2] + " " + rect[3]);
                            if(rect[0]+rect[1]+rect[2]+rect[3] > 0) {
                                calibrate_scale[0] += scale[0];
                                calibrate_rect[0] += rect[0];
                                calibrate_rect[1] += rect[1];
                                calibrate_rect[2] += rect[2];
                                calibrate_rect[3] += rect[3];
                                calibrate_num--;
                                Message tempMsg = mHandler.obtainMessage();
                                tempMsg.arg1 = 1;
                                tempMsg.what = CALIBRATE;
                                mHandler.sendMessage(tempMsg);
                            }
                            if(calibrate_num == 0) {
                                calibrate_scale[0] = calibrate_scale[0]/CALIBRATION;
                                calibrate_rect[0] = calibrate_rect[0]/CALIBRATION;
                                calibrate_rect[1] = calibrate_rect[1]/CALIBRATION;
                                calibrate_rect[2] = calibrate_rect[2]/CALIBRATION;
                                calibrate_rect[3] = calibrate_rect[3]/CALIBRATION;
                                if(calibrate_rect[0]<0)calibrate_rect[0]=0;
                                if(calibrate_rect[1]<0)calibrate_rect[1]=0;
                                if(calibrate_rect[2]>mRgbaFrame.width())calibrate_rect[2]=mRgbaFrame.width()-1;
                                if(calibrate_rect[3]>mRgbaFrame.height())calibrate_rect[3]=mRgbaFrame.height()-1;
                                Log.d("cpdebugcalrbration","calibrate scale : "+calibrate_scale[0]+" rect : "+calibrate_rect[0]+" "+calibrate_rect[1]+" "+calibrate_rect[2]+" "+calibrate_rect[3]);
                                mixController.setState(mixController.STATE_IDLE, mixController.STATE_FACE_REGISTER);
                            }

                            break;
                        default:
                            try {
                                currentThread().sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    mRgbaFrame.release();
                    mFrontFrame.release();

                }
            }
        };
        mainLoop.start();


        calibrateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDialogShow){
                    return;
                }else{
                    isDialogShow=true;
                }
                gotoCalibrate();
            }
        });

        calibrateLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        // client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public void onStart() {
        super.onStart();
        noExit=true;
    }

    @Override
    public void onStop() {
        super.onStop();
        noExit=false;
        videoUtilFront.stopPreview();
        videoUtilBack.stopPreview();
        //  onDestroy();
    }

    @Override

    public void onPause() {
        super.onPause();
//        try{
//            canCloseCam=false;
//            noExit=false;
//            while(!canCloseCam){
//                Thread.sleep(10);
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mixController.getInstance(this);
        noExit=true;
    }

    public void onDestroy() {
        super.onDestroy();
        // face.Quit();
        Log.d("morrisdebug", "destroy you ");
        mixController.setState(mixController.STATE_FACE_RECOGNIZE, mixController.STATE_FACE_REGISTER);
        noExit = false;
        face.Destroy();
        face = null;
        noExit = false;
        if (videoUtilBack != null)
            videoUtilBack.stopPreview();
        if (videoUtilFront != null)
            videoUtilFront.stopPreview();

        mainLoop = null;
        System.exit(0);
    }






    private boolean setCalibrateFromFile() {
        File calibrateFile = new File(CALIBRATE_PATH);
        if (!calibrateFile.exists()) {
            return false;
        }

        String[] calibrateStrs = FileOperator.loadCalibrate(CALIBRATE_PATH);
        if (calibrateStrs == null || calibrateStrs.length < 5) {
            return false;
        }

        calibrate_scale[0] = Float.parseFloat(calibrateStrs[0]);
        calibrate_rect[0] = Integer.parseInt(calibrateStrs[1]);
        calibrate_rect[1] = Integer.parseInt(calibrateStrs[2]);
        calibrate_rect[2] = Integer.parseInt(calibrateStrs[3]);
        calibrate_rect[3] = Integer.parseInt(calibrateStrs[4]);

        return true;
    }

    private void gotoCalibrate() {
        calibrateLayout.setVisibility(View.VISIBLE);
        calibrate_num = CALIBRATION;
        calibrate_scale[0] = 0;
        calibrate_rect[0] = 0;
        calibrate_rect[1] = 0;
        calibrate_rect[2] = 0;
        calibrate_rect[3] = 0;

        mixController.setState(mixController.STATE_CALIBRATE, mixController.STATE_IDLE);
    }

    public void copyFilesFassets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFassets(context, oldPath + "/" + fileName, newPath + "/" + fileName);

                }
            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //如果捕捉到错误则通知UI线程

        }
    }


}
