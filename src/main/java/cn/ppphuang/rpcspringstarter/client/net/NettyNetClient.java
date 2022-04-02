package cn.ppphuang.rpcspringstarter.client.net;

import cn.ppphuang.rpcspringstarter.client.net.handler.SendHandler;
import cn.ppphuang.rpcspringstarter.client.net.handler.SendHandlerV2;
import cn.ppphuang.rpcspringstarter.common.codec.LengthEncoder;
import cn.ppphuang.rpcspringstarter.common.compresser.Compresser;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.common.model.Service;
import cn.ppphuang.rpcspringstarter.common.protocol.MessageProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 客户端网络接口Netty实现
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
@Slf4j
public class NettyNetClient implements NetClient {

    private static ExecutorService threadPool = new ThreadPoolExecutor(4, 10, 200, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000), new NameTreadFactory());

    private EventLoopGroup loopGroup = new NioEventLoopGroup(4);

    /**
     * 已连接的服务缓存
     * key: 服务地址 ip:port
     */
    public static Map<String, SendHandlerV2> connectedServerNodes = new ConcurrentHashMap<>();

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
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(4096, 0, 4, 0, 4));
                            pipeline.addLast(new LengthEncoder());
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
    public RpcResponse sendRequest(RpcRequest rpcRequest, Service service, MessageProtocol messageProtocol, Compresser compresser) {
        String address = service.getAddress();
        synchronized (address) {
            if (connectedServerNodes.containsKey(address)) {
                SendHandlerV2 handlerV2 = connectedServerNodes.get(address);
                log.debug("使用现有连接");
                return handlerV2.sendRequest(rpcRequest);
            }
        }
        String[] addressInfo = address.split(":");
        final String serverAddress = addressInfo[0];
        final String serverPort = addressInfo[1];
        final SendHandlerV2 handler = new SendHandlerV2(address, messageProtocol, compresser);
        threadPool.submit(() -> {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(loopGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(4096, 0, 4, 0, 4));
                            pipeline.addLast(new LengthEncoder());
                            pipeline.addLast(handler);
                        }
                    });
            //new connect
            ChannelFuture channelFuture = bootstrap.connect(serverAddress, Integer.parseInt(serverPort));
            channelFuture.addListener((ChannelFutureListener) channelFuture1 -> connectedServerNodes.put(address, handler));
        });
        log.debug("使用新的连接。。。");
        return handler.sendRequest(rpcRequest);
    }

    static class NameTreadFactory implements ThreadFactory {

        private final AtomicInteger mThreadNum = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "rpcClient-" + mThreadNum.getAndIncrement());
            return t;
        }
    }
}


