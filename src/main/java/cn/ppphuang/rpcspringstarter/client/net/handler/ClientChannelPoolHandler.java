package cn.ppphuang.rpcspringstarter.client.net.handler;

import cn.ppphuang.rpcspringstarter.common.codec.MessageDecoder;
import cn.ppphuang.rpcspringstarter.common.codec.MessageEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 客户端网络接口Netty连接池Handler
 *
 * @Author: ppphuang
 * @Create: 2022/4/10
 */
@Slf4j
public class ClientChannelPoolHandler implements ChannelPoolHandler {

    @Override
    public void channelReleased(Channel channel) throws Exception {
        log.debug("release success address :{} channel:{}", channel.remoteAddress(), channel.id());
    }

    @Override
    public void channelAcquired(Channel channel) throws Exception {
        log.debug("acquired address :{} channel:{}", channel.remoteAddress(), channel.id());
    }

    @Override
    public void channelCreated(Channel channel) throws Exception {
        NioSocketChannel socketChannel = (NioSocketChannel) channel;
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
        pipeline.addLast(new MessageDecoder());
        pipeline.addLast(new MessageEncoder());
        pipeline.addLast(new SendBaseHandler());
        log.debug("channelCreated remoteAddress:{}", channel.remoteAddress());
    }
}
