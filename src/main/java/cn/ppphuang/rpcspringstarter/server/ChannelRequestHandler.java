package cn.ppphuang.rpcspringstarter.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
    protected RequestHandler requestHandler;

    public ChannelRequestHandler(RequestHandler requestHandler) {
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
                log.debug("the server receives message :{}", msg);
                ByteBuf byteBuf = (ByteBuf) msg;
                byte[] bytes = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(bytes);
                //回收ByteBuf
                ReferenceCountUtil.release(byteBuf);
                byte[] resp = requestHandler.handleRequest(bytes);
                ByteBuf respBuffer = Unpooled.buffer(resp.length);
                respBuffer.writeBytes(resp);
                log.debug("Send Response:{}", respBuffer);
                ctx.writeAndFlush(respBuffer);
            } catch (Exception e) {
                log.error("server read exception", e);
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
