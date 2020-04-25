package com.openailab.facetrack;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.openailab.facelibrary.FaceAPP;
import com.openailab.facetrack.ui.FullscreenActivity;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class GuideActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private int REQ_GET_CAMERA_PER = 100;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        startActivity(new Intent(this, FullscreenActivity.class));
        finish();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "请赋予程序相机与读写存储权限！", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        boolean permissions = EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA);
        if (!permissions) {
            EasyPermissions.requestPermissions(this, "请赋予程序相机与读写存储权限！", REQ_GET_CAMERA_PER, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA);
        } else {

            String oem_id ="202004079f10d931";//OEMID
            String contract_id ="0fa7";//合同号，16进制数，共四位(不足四位，高位补0)
            String password = "16036b2a9f10d93116036b37e8d98682";//用户密码
            String uidStr = oem_id+contract_id;
            int res =FaceAPP.getInstance().AuthorizedDevice(uidStr,password, this);//向鉴权服务器发送请求，返回0鉴权成功


            Log.e("liang", "鉴权:" + res);
            startActivity(new Intent(this, FullscreenActivity.class));
            finish();
        }
    }
}
