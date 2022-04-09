package cn.ppphuang.rpcspringstarter.client.net.handler;

import cn.ppphuang.rpcspringstarter.client.async.AsyncReceiveHandler;
import cn.ppphuang.rpcspringstarter.client.net.ClientProxyFactory;
import cn.ppphuang.rpcspringstarter.client.net.RpcFuture;
import cn.ppphuang.rpcspringstarter.common.constants.RpcCompressEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.constants.RpcProtocolEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcStatusEnum;
import cn.ppphuang.rpcspringstarter.common.model.RpcMessage;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.exception.RpcException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 发送数据处理类，定义Netty入站处理规则(长连接)
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
@Slf4j
public class SendHandlerV2 extends SendBaseHandler {

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        latch.countDown();
    }

    public RpcResponse sendRequest(RpcRequest request, RpcProtocolEnum messageProtocol, RpcCompressEnum compresser) {
        if (request.isAsync()) {
            return sendAsyncRequest(request, messageProtocol, compresser);
        } else {
            return sendSyncRequest(request, messageProtocol, compresser);
        }
    }

    private RpcResponse sendSyncRequest(RpcRequest request, RpcProtocolEnum messageProtocol, RpcCompressEnum compresser) {
        RpcResponse response;
        RpcFuture<RpcResponse> responseFuture = new RpcFuture<>();
        requestMap.put(request.getRequestId(), responseFuture);
        try {
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setProtocol(messageProtocol);
            rpcMessage.setCompress(compresser);
            rpcMessage.setType(RpcConstant.REQUEST_TYPE);
            rpcMessage.setData(request);
            if (latch.await(RpcConstant.CHANNEL_WAIT_TIME, TimeUnit.SECONDS)) {
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                //waiting
                response = responseFuture.get(RpcConstant.RESPONSE_WAIT_TIME, TimeUnit.SECONDS);
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

    private RpcResponse sendAsyncRequest(RpcRequest request, RpcProtocolEnum messageProtocol, RpcCompressEnum compresser) {
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
            if (latch.await(RpcConstant.CHANNEL_WAIT_TIME, TimeUnit.SECONDS)) {
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                response.setAsync(request.isAsync());
                response.setRequestId(request.getRequestId());
                response.setReturnValue(null);
                response.setRpcStatus(RpcStatusEnum.SUCCESS);
            } else {
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
