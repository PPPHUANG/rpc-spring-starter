package cn.ppphuang.rpcspringstarter.client.net.handler;

import cn.ppphuang.rpcspringstarter.client.async.AsyncReceiveHandler;
import cn.ppphuang.rpcspringstarter.client.net.NettyNetClient;
import cn.ppphuang.rpcspringstarter.client.net.RpcFuture;
import cn.ppphuang.rpcspringstarter.common.constants.RpcCompressEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.constants.RpcProtocolEnum;
import cn.ppphuang.rpcspringstarter.common.model.RpcMessage;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty返回数据处理handler
 *
 * @Author: ppphuang
 * @Create: 2022/4/9
 */
@Slf4j
public class SendBaseHandler extends ChannelInboundHandlerAdapter {

    protected volatile Channel channel;

    public static Map<String, RpcFuture<RpcResponse>> requestMap = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Connect to server successfully:{}", ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                RpcMessage message = (RpcMessage) msg;
                if (message.getType() == RpcConstant.HEARTBEAT_RESPONSE_TYPE) {
                    log.debug("receive heart response:{}", message.getData());
                } else if (message.getType() == RpcConstant.RESPONSE_TYPE) {
                    RpcResponse rpcResponse = (RpcResponse) message.getData();
                    RpcFuture<RpcResponse> rpcResponseRpcFuture = requestMap.get(rpcResponse.getRequestId());
                    //异步处理
                    if (rpcResponse.isAsync()) {
                        AsyncReceiveHandler asyncReceiveHandler = rpcResponseRpcFuture.getAsyncReceiveHandler();
                        Object asyncContext = rpcResponseRpcFuture.getAsyncContext();
                        log.debug("asyncReceiveHandler:{}", asyncReceiveHandler);
                        log.debug("asyncContext:{}", asyncContext);
                        try {
                            asyncReceiveHandler.success(asyncContext, rpcResponse);
                        } catch (Exception e) {
                            log.error("asyncReceiveHandler success method invoke error :{}", e.getMessage());
                        } finally {
                            requestMap.remove(rpcResponse.getRequestId());
                            log.debug("requestMap remove:{}", rpcResponse.getRequestId());
                        }
                    } else {
                        rpcResponseRpcFuture.setResponse(rpcResponse);
                    }
                }
            } else {
                log.error("receive error message :{} remoteAddress:{}", msg, channel.remoteAddress());
            }
        } finally {
            //手动回收
            ReferenceCountUtil.release(msg);
        }
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
        log.error("channel inactive with remoteAddress:[{}]", ctx.channel().remoteAddress());
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        NettyNetClient.connectedServerNodes.remove(socketAddress);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.debug("write idle happen [{}]", ctx.channel().remoteAddress());
                RpcMessage message = new RpcMessage();
                message.setType(RpcConstant.HEARTBEAT_REQUEST_TYPE);
                message.setCompress(RpcCompressEnum.UNZIP);
                message.setProtocol(RpcProtocolEnum.KRYO);
                channel.writeAndFlush(message).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
