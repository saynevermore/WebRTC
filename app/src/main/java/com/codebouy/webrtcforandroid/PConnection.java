package com.codebouy.webrtcforandroid;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;



public class PConnection {

    PeerConnection peerConnection;

    public VideoTrack videoTrack;

    public SurfaceViewRenderer remoteSurfaceView;

    public Callback callback;

    public PConnection() {
    }

    // 创建连接即创建一个PeerConnection
    public PeerConnection createPeerConnection(PeerConnectionFactory factory,
                                               AudioTrack audioTrack,
                                               VideoTrack videoTrack) {
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
        PeerConnection.IceServer ice_server =    //传入配置好的服务器域名及用户名密码
                PeerConnection.IceServer.builder("turn:zccbest.com:3478")
                        .setPassword("helloworld")
                        .setUsername("zcc")
                        .createIceServer();
        iceServers.add(ice_server);
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        // TCP 候选者仅在连接到支持的服务器时有用
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        rtcConfig.enableDtlsSrtp = true;
        PeerConnection connection = factory.createPeerConnection(rtcConfig, mPeerConnectionObserver);
        List<String> mediaStreamLabels = Collections.singletonList("ARDAMS");
        connection.addTrack(videoTrack, mediaStreamLabels);
        connection.addTrack(audioTrack, mediaStreamLabels);
        peerConnection = connection;
        return connection;
    }

    public void onRemoteAnswer(JSONObject jsep) throws JSONException {
        String description = jsep.getString("sdp");
        peerConnection.setRemoteDescription(new org.webrtc.SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) { }
            @Override
            public void onSetSuccess() { }
            @Override
            public void onCreateFailure(String s) { }
            @Override
            public void onSetFailure(String s) { }
        }, new SessionDescription(SessionDescription.Type.fromCanonicalForm(jsep.getString("type")), description));
    }

    public void createAnswer(final CompletionHandler completionHandler) {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        //开启音频接收
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        //开启视频接收
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        //开启Dtls(加密的UDP)
        sdpMediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        peerConnection.createAnswer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) { }
                    @Override
                    public void onSetSuccess() { }
                    @Override
                    public void onCreateFailure(String s) { }
                    @Override
                    public void onSetFailure(String s) { }      }, sessionDescription);
                completionHandler.success(sessionDescription);
            }
            @Override
            public void onSetSuccess() { }
            @Override
            public void onCreateFailure(String s) { }
            @Override
            public void onSetFailure(String s) { }
        }, sdpMediaConstraints);
    }

    // 创建offer，同时需要传入一些配置参数，通过MediaConstraints传入
    public void createOffer(final CompletionHandler completionHandler) {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        sdpMediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        peerConnection.createOffer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription s) { }//创建SDP成功
                    @Override
                    public void onSetSuccess() { }//设置{local,Remote}Description成功
                    @Override
                    public void onCreateFailure(String s) {
                        Log.i("offer", s);
                    }
                    @Override
                    public void onSetFailure(String s) {
                        Log.i("offer", s);
                    }
                }, sessionDescription);
                completionHandler.success(sessionDescription);
            }

            @Override
            public void onSetSuccess() {

            }

            @Override
            public void onCreateFailure(String s) {
                Log.i("offer", s);
            }

            @Override
            public void onSetFailure(String s) {
                Log.i("offer", s);

            }
        }, sdpMediaConstraints);
    }


    public void close() {
        peerConnection.close();
        peerConnection = null;
    }

    private PeerConnection.Observer mPeerConnectionObserver = new PeerConnection.Observer() {
        // PeerConnectionObserver是用来监听PeerConnection连接中的事件的监听者，可以用来监听一些如数据的到达、流的增加或删除等事件
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            callback.onIceCandidate(peerConnection, iceCandidate);

        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            callback.onAddStream(peerConnection, mediaStream);

        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

        }
    };


    public interface Callback {
        // 收到远端媒体流回调
        void onAddStream(
                PeerConnection peerConnection, MediaStream mediaStream);

        // 收到远端候选者回调
        void onIceCandidate(
                PeerConnection peerConnection, IceCandidate candidate);
    }

    public interface CompletionHandler {
        public void success(SessionDescription sessionDescription);
    }


}
