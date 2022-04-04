package cn.ppphuang.rpcspringstarter.server;

import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.model.RpcMessage;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.server.handler.RequestBaseHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
        NettyRpcServer.THREAD_POOL.submit(() -> {
            try {
                log.debug("the server receives decode message :{}", msg);
                RpcMessage message = (RpcMessage) msg;
                RpcResponse resp = requestHandler.handleRequest((RpcRequest) message.getData());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setProtocol(requestHandler.getProtocol());
                rpcMessage.setCompress(requestHandler.getCompresser());
                rpcMessage.setType(RpcConstant.RESPONSE_TYPE);
                rpcMessage.setData(resp);
                log.debug("Send Response:{}", rpcMessage);
                ctx.writeAndFlush(rpcMessage);
            } catch (Exception e) {
                log.error("server read exception", e);
            } finally {
                //回收ByteBuf
                ReferenceCountUtil.release(msg);
            }
        });
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
}
