package com.example.testcapture;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.Camera2Enumerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShowFragment extends Fragment {
    private SurfaceViewRenderer surfaceViewRenderer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView =
                inflater.inflate(R.layout.fragment_show, container, false);

        surfaceViewRenderer = rootView.findViewById(R.id.surfaceView);

        surfaceViewRenderer.init(GlobalData.eglRoot.getEglBaseContext(), null);
        surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        surfaceViewRenderer.setEnableHardwareScaler(false /* enabled */);
        GlobalData.videoTrack.addSink(surfaceViewRenderer);
//        if (surfaceViewRenderer != null) {
//            surfaceViewRenderer.release();
//        }
        return rootView;
    }
}
