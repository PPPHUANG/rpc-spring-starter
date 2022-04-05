package cn.ppphuang.rpcspringstarter.server;

import cn.ppphuang.rpcspringstarter.common.constants.RpcCompressEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.constants.RpcProtocolEnum;
import cn.ppphuang.rpcspringstarter.common.model.RpcMessage;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.server.handler.RequestBaseHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * netty 入站 handler
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
@Slf4j
public class ChannelRequestHandler extends ChannelInboundHandlerAdapter {

    /**
     * 请求handler
     */
    protected RequestBaseHandler requestHandler;

    public ChannelRequestHandler(RequestBaseHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel active :{}", ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("the server receives decode message :{}", msg);
        try {
            if (msg instanceof RpcMessage) {
                RpcMessage message = (RpcMessage) msg;
                if (message.getType() == RpcConstant.HEARTBEAT_REQUEST_TYPE) {
                    RpcMessage heartMessage = new RpcMessage();
                    heartMessage.setCompress(RpcCompressEnum.UNZIP);
                    heartMessage.setProtocol(RpcProtocolEnum.KRYO);
                    heartMessage.setType(RpcConstant.HEARTBEAT_RESPONSE_TYPE);
                    ctx.writeAndFlush(heartMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    NettyRpcServer.THREAD_POOL.submit(() -> {
                        try {
                            log.debug("the server receives decode message :{}", msg);
                            RpcResponse resp = requestHandler.handleRequest((RpcRequest) message.getData());
                            RpcMessage rpcMessage = new RpcMessage();
                            rpcMessage.setProtocol(requestHandler.getProtocol());
                            rpcMessage.setCompress(requestHandler.getCompresser());
                            rpcMessage.setType(RpcConstant.RESPONSE_TYPE);
                            rpcMessage.setData(resp);
                            log.debug("Send Response:{}", rpcMessage);
                            ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                            ;
                        } catch (Exception e) {
                            log.error("server read exception", e);
                        }
                    });
                }
            } else {
                log.error("receive error message :{} remoteAddress:{}", msg, ctx.channel().remoteAddress());
            }
        } finally {
            //回收ByteBuf
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
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.debug("read idle happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
