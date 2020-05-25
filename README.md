# WebRTCForAndroid：一个基于Android平台的实时视频通讯系统
# 1.系统中主要技术的介绍：
<br>（1）实现音视频通讯系统的核心技术WebRTC，包含几个重要API：PeerConnectionFactory、VideoCapture、VideoSource、MediaStream、PeerConnection。<br>
<br>（2）为传递两端SDP和Candidate信息，选取WebSocket作为信令，包中server.js文件即为信令服务器端代码。<br>
<br>（3）为保证能够在各种网络环境下维持通信，即手机设备处在私网的情况下，配置Coturn服务器实现内网穿透。<br>
# 2.完成系统主要步骤：
<br>（1）Coturn服务器搭建<br>
<br>（2）WebSocket信令服务器实现<br>
<br>（3）node环境搭建并启动sever.js服务器<br>
<br>（4）客户端第三方类的添加以及界面设计<br>
<br>（5）客户端WebSocket信令类封装<br>
<br>（6）媒体获取类MediaCapturer封装<br>
<br>（7）连接类PeerConnection封装<br>
<br>（8）CallVideoActivity创建实现通话功能<br>
# 3.软件开发的主要运行环境：
<br>（1）客户端平台：windows、Android Studio + Java、Visual Studio + JavaScript<br>
<br>（2）服务器端平台：linux/Ubuntu、Visual Studio + JavaScript、Node.js v8.10.0 + npm v3.5.2<br>
