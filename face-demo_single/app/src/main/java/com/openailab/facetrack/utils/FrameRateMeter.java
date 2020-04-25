package com.openailab.facetrack.utils;


public class FrameRateMeter {
    private float mTimes = 0F;
    private float mCurrentFps = 0F;
    private long mUpdateTime = 0L;

    private long TIMETRAVEL = 1L;
    private long TIMETRAVEL_MS = TIMETRAVEL * 1000;
    private long TIMETRAVEL_MAX_DIVIDE = 2 * TIMETRAVEL_MS;


    public float getFps() {
        if (System.currentTimeMillis() - mUpdateTime > TIMETRAVEL_MAX_DIVIDE) {
            return 0F;
        }
        return mCurrentFps;
    }

    public void drawFrameCount() {
        long currentTime = System.currentTimeMillis();
        if (mUpdateTime == 0L) {
            mUpdateTime = currentTime;
        }
        if (currentTime - mUpdateTime > TIMETRAVEL_MS) {
            mCurrentFps = mTimes / (currentTime - mUpdateTime) * 1000;
            mUpdateTime = currentTime;
            mTimes = 0F;
        }
        mTimes++;
    }

}
