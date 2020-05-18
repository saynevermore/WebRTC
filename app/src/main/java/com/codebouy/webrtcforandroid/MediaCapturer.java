package com.codebouy.webrtcforandroid;

import android.content.Context;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioUtils;



public class  MediaCapturer {                   //媒体获取类：Android端媒体设备获取

    private static final String TAG = "MediaCapturer";
    private static final String MEDIA_STREAM_ID = "ARDAMS";  //媒体流id
    private static final String VIDEO_TRACK_ID = "ARDAMSv0"; //视频轨id
    private static final String AUDIO_TRACK_ID = "ARDAMSa0"; //音频轨id

    private CameraVideoCapturer mCameraVideoCapturer;
    public final PeerConnectionFactory mPeerConnectionFactory; //下文初始化后可以创建PeerConnectionFactory实例
    private final MediaStream mMediaStream;

    public MediaCapturer(Context context, EglBase mRootEglBase) {
        this.mPeerConnectionFactory = this.createPeerConnectionFactory(context, mRootEglBase);
        this.mMediaStream = this.mPeerConnectionFactory.createLocalMediaStream(MEDIA_STREAM_ID);
    }


    //创建PeerConnectionFactory
    PeerConnectionFactory createPeerConnectionFactory(Context context, EglBase mRootEglBase) {
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;
        //编码
        encoderFactory = new DefaultVideoEncoderFactory(mRootEglBase.getEglBaseContext(),
                true /* enableIntelVp8Encoder */,
                true /* 支持android H264编码 支持硬件加速 */);
        //解码
        decoderFactory = new DefaultVideoDecoderFactory(mRootEglBase.getEglBaseContext());
        //先对PeerConnectionFactory初始化
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions());
        PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory);
        builder.setOptions(null);
        return builder.createPeerConnectionFactory();
    }

    //初始化相机的方法
    public void initCamera(Context context) throws Exception {
        boolean isCamera2Supported = Camera2Enumerator.isSupported(context);
        CameraEnumerator cameraEnumerator;
        if (isCamera2Supported) {
            cameraEnumerator = new Camera2Enumerator(context);
        } else {
            cameraEnumerator = new Camera1Enumerator();
        }
        final String[] deviceNames = cameraEnumerator.getDeviceNames();  //获取照相机的名字
        //获取照相机中的前置摄像头
        for (String deviceName : deviceNames) {
            // Get the front camera for now
            if (cameraEnumerator.isFrontFacing(deviceName)) {
                mCameraVideoCapturer = cameraEnumerator.createCapturer(deviceName, new MediaCapturerEventHandler());
                Log.d(TAG, "created camera video capturer deviceName=" + deviceName);
            }
        }
        if (mCameraVideoCapturer == null) {
            throw new Exception("Failed to get Camera Device");
        }
    }

    //创建视频数据源(先指定采集视频数据的设备,然后使用观察者模式从指定设备中获取数据)
    public VideoTrack createVideoTrack(Context context,
                                       SurfaceViewRenderer localVideoView,
                                       EglBase.Context eglBaseContext) {

        if (mCameraVideoCapturer == null) {
            throw new IllegalStateException("Camera must be initialized");
        }
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext);
        VideoSource videoSource = mPeerConnectionFactory.createVideoSource(false);
        //在初始化 VideoCaptuer 的时候，可以过观察者模式将 VideoCapture 与 VideoSource 联接到了一起。
        mCameraVideoCapturer.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());
        //调用VideoCaptuer对象的 startCapture方法真正的打开摄像头，camera开始工作
        mCameraVideoCapturer.startCapture(640, 480, 30);
        VideoTrack videoTrack = mPeerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        videoTrack.setEnabled(true);
        localVideoView.setMirror(true);  //设置图像显示时反转，不然视频显示的内容与实际内容正好相反
        localVideoView.setEnableHardwareScaler(true); //打开便件进行拉伸
        mMediaStream.addTrack(videoTrack);//将捕获到的视频添加入媒体流
        videoTrack.addSink(localVideoView); //将从摄像头采集的数据设置到该view里就可以显示了
        return videoTrack;
    }

    //创建声音数据源(创建AudioSource时,就开始从音频设备捕获数据)
    public AudioTrack createAudioTrack() {
        AudioSource audioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
        WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
        WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true);
        AudioTrack audioTrack = mPeerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        audioTrack.setEnabled(true);
        mMediaStream.addTrack(audioTrack);
        return audioTrack;
    }

    //android对于照相机当前状态的回调
    private class MediaCapturerEventHandler implements CameraVideoCapturer.CameraEventsHandler {
        @Override
        public void onCameraOpening(String s) {
            Log.d(TAG, "onCameraOpening s=" + s);
        }

        @Override
        public void onFirstFrameAvailable() {
            Log.d(TAG, "onFirstFrameAvailable");
        }

        @Override
        public void onCameraFreezed(String s) {
            Log.d(TAG, "onCameraFreezed s=" + s);
        }

        @Override
        public void onCameraError(String s) {
            Log.e(TAG, "onCameraError s=" + s);
        }

        @Override
        public void onCameraDisconnected() {
            Log.d(TAG, "onCameraDisconnected");
        }

        @Override
        public void onCameraClosed() {
            Log.d(TAG, "onCameraClosed");
        }
    }

}




