package cn.ppphuang.rpcspringstarter.server;

import cn.ppphuang.rpcspringstarter.common.codec.LengthEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
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

    public static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(4, 8, 200, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000), new NameTreadFactory());


    public NettyRpcServer(int port, String protocol, RequestHandler requestHandler) {
        super(port, protocol, requestHandler);
    }

    @Override
    public void start() {
        //配置netty
        NioEventLoopGroup boosGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boosGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(4096, 0, 4, 0, 4));
                            pipeline.addLast(new LengthEncoder());
                            pipeline.addLast(new ChannelRequestHandler(requestHandler));
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
