package cn.ppphuang.rpcspringstarter.server;

import cn.ppphuang.rpcspringstarter.common.codec.MessageDecoder;
import cn.ppphuang.rpcspringstarter.common.codec.MessageEncoder;
import cn.ppphuang.rpcspringstarter.server.handler.RequestBaseHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 服务RpcServer实现
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
@Slf4j
public class NettyRpcServer extends RpcServer {

    private Channel channel;

    public NettyRpcServer(int port, String protocol, RequestBaseHandler requestHandler) {
        super(port, protocol, requestHandler);
    }

    @Override
    public void start() {
        //配置netty
        NioEventLoopGroup boosGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                    Runtime.getRuntime().availableProcessors() * 2
            );
            serverBootstrap.group(boosGroup, workerGroup).channel(NioServerSocketChannel.class)
                    //三次握手连接队列（accept queue） 存放已完成三次握手的请求的队列的最大长度
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new IdleStateHandler(15, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new MessageDecoder());
                            pipeline.addLast(new MessageEncoder());
                            pipeline.addLast(serviceHandlerGroup, new ChannelRequestHandler(requestHandler));
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(port).sync();
            log.debug("Server started successfully.");
            channel = future.channel();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("start netty server failed, message: {}", e.getMessage());
        } finally {
            log.debug("shutdownGracefully");
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {
        channel.close();
        log.debug("Server stop successfully.");
    }

    static class NameTreadFactory implements ThreadFactory {

        private final AtomicInteger mThreadNum = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "rpcServer-" + mThreadNum.getAndIncrement());
            return t;
        }
    }
}
