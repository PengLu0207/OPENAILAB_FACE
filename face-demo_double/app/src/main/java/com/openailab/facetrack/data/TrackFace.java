package com.openailab.facetrack.data;

import com.openailab.facelibrary.FaceInfo;

import org.opencv.core.Mat;

public class TrackFace {

    public int id;
    public Mat facemat;
    public FaceInfo info = new FaceInfo();
    public String name;
    public int newFlag;
}
