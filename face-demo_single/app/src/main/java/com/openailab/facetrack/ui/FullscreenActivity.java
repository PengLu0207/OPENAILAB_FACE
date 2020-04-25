package com.openailab.facetrack.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.openailab.facelibrary.FaceAPP;
import com.openailab.facetrack.R;
import com.openailab.facetrack.utils.CameraUtils;
import com.openailab.facetrack.utils.FaceUtils;
import com.openailab.facetrack.widget.InfoView;
import com.openailab.facetrack.widget.RectView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


public class FullscreenActivity extends AppCompatActivity {
    private FaceAPP mFace = FaceAPP.getInstance();
    private static final boolean AUTO_HIDE = true;

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private static final int UI_ANIMATION_DELAY = 300;
    private View mContentView;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };


    private View mControlsView;
    private RectView mRectView;
    private InfoView mInfoView;

    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.root);

        mRectView = findViewById(R.id.rectView);
        mInfoView = findViewById(R.id.infoView);
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        View register = findViewById(R.id.register);
        final AlertDialog dialog = registerDialog();
        register.setOnTouchListener(mDelayHideTouchListener);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
        initFunction();
        TextView authStatusText = findViewById(R.id.authStatusText);
        authStatusText.setText("鉴权状态："+mFace.getAuthStatus());
    }

    private void initFunction() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final int widthPixels = displayMetrics.widthPixels;
        final int heightPixels = displayMetrics.heightPixels;
        mFace.SetDBName(getFilesDir().getAbsolutePath() + "/facedb.dat");
        copyFilesAssets("openailab", "/mnt/sdcard/openailab");
        final  String[] params = {"a", "b", "c", "d",
                "factor", "min_size", "clarity", "perfoptimize", "livenessdetect", "gray2colorScale", "frame_num", "quality_thresh", "mode", "facenum","liveness_thresh","iou_thresh"};
        final double[] value = new double[]{
                0.6, 0.7, 0.7, 0.63,
                0.709, 40, 10, 0, 0, 0.5, 1, 0.8, 1, 1,0.9,0.05
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                mFace.SetParameter(params, value);
                mFace.OpenDB();
                while (true) {
                    FaceUtils.getInstance().livess(mRectView, mInfoView, widthPixels, heightPixels);
                }
            }
        }).start();
    }


    private AlertDialog registerDialog() {
        View view = View.inflate(this, R.layout.dialog_register, null);
        final EditText editText = view.findViewById(R.id.etName);
        return new AlertDialog.Builder(this).setTitle("人脸注册！")
                .setIcon(android.R.drawable.ic_input_add)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("注册", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = editText.getText().toString();
                        int register = FaceUtils.getInstance().register(name);
                        Toast.makeText(FullscreenActivity.this, register == 0 ? "注册成功" : "注册失败", Toast.LENGTH_LONG).show();
                        editText.setText("");
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editText.setText("");
                    }
                }).create();
    }

    private void copyFilesAssets(String oldPath, String newPath) {
        try {
            String[] fileNames = getAssets().list(oldPath);
            if (fileNames.length != 0) {
                File file = new File(newPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                for (String fileName : fileNames) {
                    copyFilesAssets(oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {
                InputStream inputStream = getAssets().open(oldPath);
                FileOutputStream fileOutputStream = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, byteCount);
                }
                fileOutputStream.flush();
                inputStream.close();
                fileOutputStream.close();
            }
        } catch (
                Exception e) {
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
//        CameraUtils.startPreview();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        CameraUtils.startPreview();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
        }

        @Override
        public void onPackageInstall(int operation, InstallCallbackInterface callback) {
            super.onPackageInstall(operation, callback);
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


}
