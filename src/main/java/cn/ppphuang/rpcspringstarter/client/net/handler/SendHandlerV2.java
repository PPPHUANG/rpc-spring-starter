package cn.ppphuang.rpcspringstarter.client.net.handler;

import cn.ppphuang.rpcspringstarter.client.net.NettyNetClient;
import cn.ppphuang.rpcspringstarter.client.net.RpcFuture;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.common.protocol.MessageProtocol;
import cn.ppphuang.rpcspringstarter.exception.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 发送数据处理类，定义Netty入站处理规则(长连接)
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
@Slf4j
public class SendHandlerV2 extends ChannelInboundHandlerAdapter {
    /**
     * 等待通道简历超时时间
     */
    static final int CHANNEL_WAIT_TIME = 4;

    /**
     * 等待响应最大时间
     */
    static final int RESPONSE_WAIT_TIME = 8;

    private volatile Channel channel;

    private String remoteAddress;

    private static Map<String, RpcFuture<RpcResponse>> requestMap = new ConcurrentHashMap<>();

    private MessageProtocol messageProtocol;

    private CountDownLatch latch = new CountDownLatch(1);

    public SendHandlerV2(String remoteAddress, MessageProtocol messageProtocol) {
        this.remoteAddress = remoteAddress;
        this.messageProtocol = messageProtocol;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Connect to server successfully:{}", ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
        latch.countDown();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("Client read message:{}", msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] response = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(response);
        //手动回收
        ReferenceCountUtil.release(byteBuf);
        RpcResponse rpcResponse = messageProtocol.unmarshallingResponse(response);
        RpcFuture<RpcResponse> rpcResponseRpcFuture = requestMap.get(rpcResponse.getRequestId());
        rpcResponseRpcFuture.setResponse(rpcResponse);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        log.error("Exception occurred:{}", cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.error("channel inactive with remoteAddress:[{}]", remoteAddress);
        NettyNetClient.connectedServerNodes.remove(remoteAddress);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    public RpcResponse sendRequest(RpcRequest request) {
        RpcResponse response;
        RpcFuture<RpcResponse> responseFuture = new RpcFuture<>();
        requestMap.put(request.getRequestId(), responseFuture);
        try {
            byte[] data = messageProtocol.marshallingRequest(request);
            ByteBuf buffer = Unpooled.buffer(data.length);
            buffer.writeBytes(data);
            if (latch.await(CHANNEL_WAIT_TIME, TimeUnit.SECONDS)) {
                channel.writeAndFlush(buffer);
                //waiting
                response = responseFuture.get(RESPONSE_WAIT_TIME, TimeUnit.SECONDS);
            } else {
                throw new RpcException("establish channel time out");
            }
        } catch (Exception e) {
            throw new RpcException(e.getMessage());
        } finally {
            requestMap.remove(request.getRequestId());
        }
        return response;
    }
}
