// 引入websocket包  node+ws模块实现WebSocket
const websocket = require('ws');
const ws = new websocket.Server({ port: 7080 }, () => {
    console.log("ws: 0.0.0.0:" + 7080);
});
// 保存连接socket对象的set容器
var clients = new Set();  
// 保存会话的sesssion容器
var sessions = [];

// 刷新房间内人员信息
function updatePeers() {
    var peers = [];
    clients.forEach(function (client) {  
        var peer = {};
        if (client.hasOwnProperty('id')) {  
            peer.id = client.id;
        }
        if (client.hasOwnProperty('name')) {
            peer.name = client.name;
        }
        if (client.hasOwnProperty('session_id')) {
            peer.session_id = client.session_id;
        }
        peers.push(peer);
    });
    var msg = {   
        type: "peers",
        data: peers
    };
    clients.forEach(function (client) {     
        send(client, JSON.stringify(msg));  
    });
}

// 连接处理事件，有客户端接入进来:WebSocket对象调用on()方法开启某一事件，第一个参数表示发生的事件，第二个传入一个方法
ws.on('connection', function connection(client_self) {  //连接后传入一个socket对象=client_self
    clients.add(client_self);  //放入clients容器中，每一次连接都会存入一个

    //收到消息处理事件
    client_self.on('message', function (message) { 
        try {
            message = JSON.parse(message);   //收到传来的message后 使用JSON.parse()方法将json字符串转换为JavaScript对象(JSON解析)
            console.log("message.type::: " + message.type + ", \n body: " + JSON.stringify(message)); //解析成功查看解析消息的类型和消息体的类型

        } catch (e) {
            console.log(e.message);
        }

        switch (message.type) {   //在客户端向服务器端发送信息时会有很多种类型
            // 1.新成员加入
            case 'new':
                {
                    client_self.id = "" + message.id;  //每一个id对应客户端id
                    client_self.name = message.name;
                    client_self.user_agent = message.user_agent;  //使服务器端识别客户端使用的操作系统、版本、语言等等
                    // 向客户端发送有新用户进入房间,需要刷新
                    updatePeers();
                }
                break;

            // 2.离开房间：退出1对1通讯
            case 'bye':
                {
                    var session = null;
                    sessions.forEach((sess) => {
                        if (sess.id == message.session_id) {
                            session = sess;
                        }

                    });

                    if (!session) {
                        var msg = {    //传入一个错误信息,因为当前是无对话的所以传入当前消息是无效的
                            type: "error", data: {
                                error: "Invalid session" + message.session_id,
                            }
                        };
                        send(client_self, JSON.stringify(msg));
                        return;
                    }

                    clients.forEach((client) => {     //遍历当前房间内用户,对bye消息完成一个转发
                        if (client.session_id === message.session_id) {
                            var msg = {
                                type: "bye",
                                data: {
                                    session_id: message.session_id,
                                    from: message.from,
                                    to: (client.id == session.from ? session.to : session.from),
                                }
                            };
                            send(client,JSON.stringify(msg));  //发送消息体

                        }
                    });

                    break;
                }
            // 转发offer：A端向B端发送offer
            case "offer": {
                var peer = null;
                clients.forEach(function (client) {
                    if (client.hasOwnProperty('id') && client.id === "" + message.to) {  //校验id；三等号需要严格比较两个值的类型
                        peer = client;   //若找到了该对象，就赋给临时变量peer
                    }
                });
                if (peer != null) {
                    msg = {
                        type: "offer",
                        data: {
                            to: peer.id,           //接收方
                            from: client_self.id,  //发送方
                            session_id: message.session_id,
                            description: message.description,
                        }
                    }
                    send(peer, JSON.stringify(msg));

                    peer.session_id = message.session_id;
                    client_self.session_id = message.session_id;

                    let session = {   //组织session信息
                        id: message.session_id,
                        from: client_self.id,
                        to: peer.id
                    };
                    sessions.push(session);
                }
            }
                break;
            // 转发answer：B端向A端发送answer
            case 'answer':
                {
                    var msg = {
                        type: "answer",
                        data: {
                            to: message.to,    
                            from: client_self.id,
                            description: message.description,
                        }
                    };

                    clients.forEach(function (client) {       
                        if (client.id === "" + message.to &&
                            client.session_id === message.session_id) {
                            send(client, JSON.stringify(msg));
                        }
                    });
                }
                break;

            // 收到候选者转发 candidate ：   A端在发送offer的同时会获取候选者candidate，然后也会向B端发送candidate
            case 'candidate':
                {
                    var msg = {
                        type: "candidate",
                        data: {
                            from: client_self.id,
                            to: message.to,
                            candidate: message.candidate
                        }
                    };

                    clients.forEach(function (client) {
                        if (client.id === "" + message.to &&
                            client.session_id === message.session_id) {
                            send(client, JSON.stringify(msg));
                        }
                    });
                }
                break;
            // keepalive 心跳 ： A端与B端保持连接   
            // WebSocket协议是基于TCP连接的，所以TCP的keepalive是侧重在保持客户端和服务端的连接，
            // 一方会不定期发送心跳包给另一方，当一方端掉的时候，没有断掉的定时发送几次心跳包，
            // 如果间隔发送几次，对方都返回的是RST，而不是ACK，那么就释放当前链接。
            // 设想一下，如果tcp层没有keepalive的机制，一旦一方断开连接却没有发送FIN给另外一方的话，
            // 那么另外一方会一直以为这个连接还是存活的，几天，几月。那么这对服务器资源的影响是很大的。
            case "keepalive":
                {
                    send(client_self, JSON.stringify({ type: 'keepalive', data: {} }));
                }
                break;
        }
    });
});


// 发送消息
function send(client, message) {
    try {
        client.send(message);
    } catch (e) {
        console.log("Send failure !:" + e);
    }

}