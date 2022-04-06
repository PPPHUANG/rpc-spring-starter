package cn.ppphuang.rpcspringstarter.client.net.handler;

import cn.ppphuang.rpcspringstarter.client.async.AsyncReceiveHandler;
import cn.ppphuang.rpcspringstarter.client.net.ClientProxyFactory;
import cn.ppphuang.rpcspringstarter.client.net.NettyNetClient;
import cn.ppphuang.rpcspringstarter.client.net.RpcFuture;
import cn.ppphuang.rpcspringstarter.common.constants.RpcCompressEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.constants.RpcProtocolEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcStatusEnum;
import cn.ppphuang.rpcspringstarter.common.model.RpcMessage;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.exception.RpcException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
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
     * 等待通道建立超时时间
     */
    static final int CHANNEL_WAIT_TIME = 4;

    /**
     * 等待响应最大时间
     */
    static final int RESPONSE_WAIT_TIME = 8;

    private volatile Channel channel;

    private String remoteAddress;

    private static Map<String, RpcFuture<RpcResponse>> requestMap = new ConcurrentHashMap<>();

    private RpcProtocolEnum messageProtocol;

    private RpcCompressEnum compresser;

    private CountDownLatch latch = new CountDownLatch(1);

    public SendHandlerV2(String remoteAddress, RpcProtocolEnum messageProtocol, RpcCompressEnum compresser) {
        this.remoteAddress = remoteAddress;
        this.messageProtocol = messageProtocol;
        this.compresser = compresser;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Connect to server successfully:{}", ctx);
        this.channel = ctx.channel();
        latch.countDown();
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
        log.error("channel inactive with remoteAddress:[{}]", remoteAddress);
        NettyNetClient.connectedServerNodes.remove(remoteAddress);
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

    public RpcResponse sendRequest(RpcRequest request) {
        if (request.isAsync()) {
            return sendAsyncRequest(request);
        } else {
            return sendSyncRequest(request);
        }
    }

    public RpcResponse sendSyncRequest(RpcRequest request) {
        RpcResponse response;
        RpcFuture<RpcResponse> responseFuture = new RpcFuture<>();
        requestMap.put(request.getRequestId(), responseFuture);
        try {
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setProtocol(messageProtocol);
            rpcMessage.setCompress(compresser);
            rpcMessage.setType(RpcConstant.REQUEST_TYPE);
            rpcMessage.setData(request);
            if (latch.await(CHANNEL_WAIT_TIME, TimeUnit.SECONDS)) {
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                //waiting
                response = responseFuture.get(RESPONSE_WAIT_TIME, TimeUnit.SECONDS);
            } else {
                throw new RpcException("establish channel time out");
            }
        } catch (Exception e) {
            throw new RpcException(e.getMessage());
        } finally {
            log.debug("requestMap remove:{}", request.getRequestId());
            requestMap.remove(request.getRequestId());
        }
        return response;
    }

    public RpcResponse sendAsyncRequest(RpcRequest request) {
        RpcResponse response = new RpcResponse();
        RpcFuture<RpcResponse> responseFuture = new RpcFuture<>();
        Object asyncContext = ClientProxyFactory.getAsyncContext();
        AsyncReceiveHandler asyncReceiveHandler = ClientProxyFactory.getAsyncReceiveHandler();
        responseFuture.setAsyncReceiveHandler(asyncReceiveHandler);
        responseFuture.setAsyncContext(asyncContext);
        requestMap.put(request.getRequestId(), responseFuture);
        try {
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setProtocol(messageProtocol);
            rpcMessage.setCompress(compresser);
            rpcMessage.setType(RpcConstant.REQUEST_TYPE);
            rpcMessage.setData(request);
            if (latch.await(CHANNEL_WAIT_TIME, TimeUnit.SECONDS)) {
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                response.setAsync(request.isAsync());
                response.setRequestId(request.getRequestId());
                response.setReturnValue(null);
                response.setRpcStatus(RpcStatusEnum.SUCCESS);
            } else {
                requestMap.remove(request.getRequestId());
                log.debug("requestMap remove:{}", request.getRequestId());
                throw new RpcException("establish channel time out");
            }
        } catch (Exception e) {
            requestMap.remove(request.getRequestId());
            log.debug("requestMap remove:{}", request.getRequestId());
            throw new RpcException(e.getMessage());
        }
        return response;
    }
}
