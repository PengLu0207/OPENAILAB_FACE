package com.openailab.sdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.openailab.facelibrary.FaceAPP;


/**
 * created by LiGuang
 * on 2018/9/28
 */
public class LoginActivity extends Activity {

    TextView login_text;
    MyHandler mHandler;
    private FaceAPP face = FaceAPP.GetInstance();
    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private boolean hasCameraPermission;
    private boolean hasExtSDPermission;
    private final static String ROOT_DIR = "/sdcard/openailab/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        login_text =(TextView)this.findViewById(R.id.login_text);

        hasCameraPermission = isCameraUseable();
        while (!hasCameraPermission) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hasCameraPermission =isCameraUseable();
        }

        FileOperator.createFolder(ROOT_DIR);


        mHandler = new MyHandler();

        new Thread() {
            @Override
            public void run() {
                super.run();
                authenticationResult();
            }
        }.start();



//        Intent intent = new Intent(LoginActivity.this,
//                MainActivity.class);
//        LoginActivity.this.startActivity(intent);
//        LoginActivity.this.finish();

    }


    private void authenticationResult() {
        Message tempMsg =null;

        Log.d("zheng", "login start!");

        String oem_id ="1000000000000001";//OEMID
        String contract_id ="0001";//合同号
        String password = "0123456789abcdef0123456789abcdef";//初始授权密码
        String uidStr = oem_id+contract_id;
        int res =face.AuthorizedDevice(uidStr,password, LoginActivity.this);

        Log.d("zheng", "auth res:"+res);

        if (res == 0) {
            Log.d("zheng", "auth is success!");
            tempMsg = mHandler.obtainMessage();
            tempMsg.what = 1;
            mHandler.sendMessage(tempMsg);
            return;
        }else {
            tempMsg = mHandler.obtainMessage();
            tempMsg.what = 2;
            mHandler.sendMessage(tempMsg);
        }
    }


    class MyHandler extends Handler {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("zheng","msg.what:"+msg.what);

            switch (msg.what) {
                case 1: {
                    login_text.setText("鉴权成功！正在启动SDK......");
                }
                break;
                case 2: {
                    login_text.setText("鉴权失败！正在启动SDK......");
                }
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(LoginActivity.this,
                    MainActivity.class);
            LoginActivity.this.startActivity(intent);
            LoginActivity.this.finish();
        }
    }

    public static boolean isCameraUseable() {
        boolean canUse =true;
        Camera mCamera =null;
        try{
            mCamera = Camera.open();
// setParameters 是针对魅族MX5。MX5通过Camera.open()拿到的Camera对象不为null
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        }catch(Exception e) {
            canUse =false;
        }
        if(mCamera !=null) {
            mCamera.release();
        }
        return canUse;
    }



}
