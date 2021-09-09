package cn.ppphuang.rpcspringstarter.client.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

/**
 * 发送数据处理类，定义Netty入站处理规则
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
@Slf4j
public class SendHandler extends ChannelInboundHandlerAdapter {

    private CountDownLatch cdl;

    private Object readMsg;

    private byte[] data;

    public SendHandler(byte[] data) {
        cdl = new CountDownLatch(1);
        this.data = data;
    }

    /**
     * 连接服务后立即发送数据
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Connect to server successfully:{}", ctx);
        ByteBuf buffer = Unpooled.buffer(data.length);
        buffer.writeBytes(data);
        log.debug("Client send message:{}", buffer);
        ctx.writeAndFlush(buffer);
    }

    /**
     * 读取数据，之后释放cd锁
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("Client read message:{}", msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        //手动回收
        ReferenceCountUtil.release(byteBuf);
        readMsg = bytes;
        cdl.countDown();
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

    public Object respData() throws InterruptedException {
        cdl.await();
        return readMsg;
    }
}
