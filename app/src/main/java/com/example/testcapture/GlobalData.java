package com.example.testcapture;

import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoTrack;

public class GlobalData {
    public static PeerConnectionFactory factory;
    public static PeerConnection connection;
    public static EglBase eglRoot = EglBase.create();
    public static SdpObserver offerObserver;
    public static SdpObserver localObserver;
    public static SdpObserver remoteObserver;
    public static String localData = "";
    public static MediaConstraints sdpMediaConstraints;
    public static VideoTrack videoTrack;
    public static MediaStream videoStream;
    public static VideoCapturer videoCapturer;
}
