package com.example.testcapture;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FragmentTransaction fTrans;
    private ShowFragment showFragment;
    private PeerObserver observer;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.editTextView);
        showFragment = new ShowFragment();
        DefaultVideoDecoderFactory defaultVideoDecoderFactory = new DefaultVideoDecoderFactory(GlobalData.eglRoot.getEglBaseContext());
        DefaultVideoEncoderFactory defaultVideoEncoderFactory =
                new DefaultVideoEncoderFactory(GlobalData.eglRoot.getEglBaseContext(), false, false);
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(getApplicationContext()).createInitializationOptions());
        initObservers();
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        GlobalData.factory = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setOptions(options)
                .createPeerConnectionFactory();
        observer = new PeerObserver();

        GlobalData.videoCapturer = createCameraCapturer(new Camera2Enumerator(getApplicationContext()));
        VideoSource videoSource = GlobalData.factory.createVideoSource(false);
        GlobalData.videoTrack = GlobalData.factory.createVideoTrack("ARDAMSv0", videoSource);
        GlobalData.videoTrack.setEnabled(true);
        GlobalData.videoCapturer.initialize(SurfaceTextureHelper.create("CaptureThread", GlobalData.eglRoot.getEglBaseContext()), getApplicationContext(), videoSource.getCapturerObserver());
        GlobalData.videoCapturer.startCapture(200, 300, 30);
        GlobalData.videoTrack = GlobalData.factory.createVideoTrack("ARDAMSv0", videoSource);
        GlobalData.videoStream = GlobalData.factory.createLocalMediaStream("ARDAMS");
        GlobalData.videoStream.addTrack(GlobalData.videoTrack);
        GlobalData.sdpMediaConstraints = new MediaConstraints();
        GlobalData.connection = GlobalData.factory.createPeerConnection(getConfig(), observer);
        GlobalData.connection.addStream(GlobalData.videoStream);
        GlobalData.connection.createOffer(GlobalData.offerObserver, GlobalData.sdpMediaConstraints);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void copyOffer(View v) {
        ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE) ;
        String bs64 = android.util.Base64.encodeToString(GlobalData.localData.getBytes(), android.util.Base64.DEFAULT);
        ClipData clip = ClipData.newPlainText("Copied Text",
                bs64.replaceAll("\n", "").replaceAll("\r", ""));
        clipboard.setPrimaryClip(clip);
    }

    public void onClick(View v) {

        byte[] array  = android.util.Base64.decode(editText.getText().toString(), android.util.Base64.DEFAULT);
        String json = new String(array);
        SdpObject sdpObject;
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            sdpObject = gson.fromJson(json, SdpObject.class);
            SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, sdpObject.sdp);
            setRemoteDescription(sdp);
        } catch (Exception e) {

        }

        fTrans = getSupportFragmentManager().beginTransaction();
        switch (v.getId()) {
            case R.id.buttonShow:
                fTrans.add(R.id.frgmCont, showFragment);
                break;
            default:
                break;
        }
        fTrans.commit();
    }

    private void setRemoteDescription(SessionDescription sdp) {
        try {
            GlobalData.connection.setRemoteDescription(GlobalData.remoteObserver, sdp);
            GlobalData.connection.createAnswer(GlobalData.remoteObserver, GlobalData.sdpMediaConstraints);
        } catch (Exception e) {
            String test = e.getMessage();
        }
    }

    private PeerConnection.RTCConfiguration getConfig()  {
        List<PeerConnection.IceServer> iceServers = new ArrayList<PeerConnection.IceServer>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
        return new PeerConnection.RTCConfiguration(iceServers);
    }

    private void initObservers() {
        GlobalData.offerObserver = new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                GlobalData.connection.setLocalDescription(GlobalData.localObserver, sessionDescription);
                String str = Utils.getString(sessionDescription.description);
                GlobalData.localData = "{ \"type\": \"" + sessionDescription.type.toString().toLowerCase() + "\", \"sdp\": \"" + str + "\" }";
            }

            @Override
            public void onSetSuccess() {
                String test = "";
            }

            @Override
            public void onCreateFailure(String s) {
                String test = s;
            }

            @Override
            public void onSetFailure(String s) {
                String test = s;
            }
        };
        GlobalData.localObserver = new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                String test = "";
            }

            @Override
            public void onSetSuccess() {
                String test = "";
            }

            @Override
            public void onCreateFailure(String s) {
                String test = s;
            }

            @Override
            public void onSetFailure(String s) {
                String test = s;
            }
        };
        GlobalData.remoteObserver = new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                String test = sessionDescription.description;
            }

            @Override
            public void onSetSuccess() {

            }

            @Override
            public void onCreateFailure(String s) {
                String test = s;
            }

            @Override
            public void onSetFailure(String s) {
                String test = s;
            }
        };
    }


    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        return createBackCameraCapturer(enumerator);
    }

    private VideoCapturer createBackCameraCapturer(CameraEnumerator enumerator) {
        String[] deviceNames = enumerator.getDeviceNames();

        for (String deviceName: deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }
}
