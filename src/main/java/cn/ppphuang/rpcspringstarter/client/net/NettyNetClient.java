package cn.ppphuang.rpcspringstarter.client.net;

import cn.ppphuang.rpcspringstarter.client.net.handler.SendHandler;
import cn.ppphuang.rpcspringstarter.client.net.handler.SendHandlerV2;
import cn.ppphuang.rpcspringstarter.common.codec.MessageDecoder;
import cn.ppphuang.rpcspringstarter.common.codec.MessageEncoder;
import cn.ppphuang.rpcspringstarter.common.constants.RpcCompressEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcProtocolEnum;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.common.model.Service;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 客户端网络接口Netty实现
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
@Slf4j
public class NettyNetClient implements NetClient {
    private EventLoopGroup loopGroup = new NioEventLoopGroup(4);

    /**
     * 已连接的服务缓存
     * key: 服务地址 InetSocketAddress ip:port
     */
    public static Map<InetSocketAddress, SendHandlerV2> connectedServerNodes = new ConcurrentHashMap<>();

    @Override
    public byte[] sendRequest(byte[] data, Service service) throws InterruptedException {
        String address = service.getAddress();
        String[] addInfo = address.split(":");
        final String serverAddress = addInfo[0];
        final String serverPort = addInfo[1];
        SendHandler sendHandler = new SendHandler(data);
        byte[] respData;
        // init client
        NioEventLoopGroup loopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(loopGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new MessageDecoder());
                            pipeline.addLast(new MessageEncoder());
                            pipeline.addLast(sendHandler);
                        }
                    });
            // start client
            bootstrap.connect(serverAddress, Integer.parseInt(serverPort)).sync();
            respData = (byte[]) sendHandler.respData();
            log.debug("SendRequest get reply:{}", respData);
        } finally {
            loopGroup.shutdownGracefully();
        }
        return respData;
    }

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest, Service service, RpcProtocolEnum messageProtocol, RpcCompressEnum compresser) {
        String address = service.getAddress();
        String[] addressInfo = address.split(":");
        final String serverAddress = addressInfo[0];
        final String serverPort = addressInfo[1];
        InetSocketAddress inetSocketAddress = InetSocketAddress.createUnresolved(serverAddress, Integer.parseInt(serverPort));
        synchronized (address) {
            if (connectedServerNodes.containsKey(inetSocketAddress)) {
                SendHandlerV2 handlerV2 = connectedServerNodes.get(inetSocketAddress);
                log.debug("使用现有连接");
                return handlerV2.sendRequest(rpcRequest, messageProtocol, compresser);
            }
        }
        final SendHandlerV2 handler = new SendHandlerV2();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(loopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new MessageEncoder());
                        pipeline.addLast(handler);
                    }
                });
        //new connect
        ChannelFuture channelFuture = bootstrap.connect(serverAddress, Integer.parseInt(serverPort));
        channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
            connectedServerNodes.put(inetSocketAddress, handler);
        });
        log.debug("使用新的连接。。。");
        return handler.sendRequest(rpcRequest, messageProtocol, compresser);
    }
}


