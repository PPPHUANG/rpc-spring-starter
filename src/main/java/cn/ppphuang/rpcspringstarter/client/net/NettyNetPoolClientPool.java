package cn.ppphuang.rpcspringstarter.client.net;

import cn.ppphuang.rpcspringstarter.client.async.AsyncReceiveHandler;
import cn.ppphuang.rpcspringstarter.client.net.handler.ClientChannelPoolHandler;
import cn.ppphuang.rpcspringstarter.client.net.handler.SendBaseHandler;
import cn.ppphuang.rpcspringstarter.common.constants.RpcCompressEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.constants.RpcProtocolEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcStatusEnum;
import cn.ppphuang.rpcspringstarter.common.model.RpcMessage;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.common.model.Service;
import cn.ppphuang.rpcspringstarter.exception.RpcException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 客户端网络接口Netty连接池
 *
 * @Author: ppphuang
 * @Create: 2022/4/10
 */
@Slf4j
public class NettyNetPoolClientPool implements NetClient {

    private EventLoopGroup loopGroup = new NioEventLoopGroup();

    public ChannelPoolMap<InetSocketAddress, SimpleChannelPool> connectedServerNodesPool;

    public NettyNetPoolClientPool(Integer maxConnections, boolean newOnAcquireTimeoutAction) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(loopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true);
        connectedServerNodesPool = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
            @Override
            protected SimpleChannelPool newPool(InetSocketAddress inetSocketAddress) {
                //executor用于执行获取连接和释放连接的EventLoop。
                //maxConnections连接池中的最大连接数。
                //acquireTimeoutNanos等待连接池连接的最大时间，单位毫秒。
                //maxPendingAcquires在请求获取/建立连接大于maxConnections数时，创建等待建立连接的最大定时任务数量。例如maxConnections=2，此时已经建立了2连接，但是没有放入到连接池中，接下来的请求就会放入到一个后台执行的定时任务中，如果到了时间连接池中还没有连接，就可以建立不大于maxPendingAcquires的连接数，如果连接池中有连接了就从连接池中获取。
                //TimeoutTask.FAIL：如果连接池中没有可用连接了，等待acquireTimeoutNanos后，抛出一个超时异常。
                //TimeoutTask.NEW：如果连接池中没有可用连接了，等待acquireTimeoutNanos后，创建一个新的连接。
                //releaseHealthCheck表示在获取连接或者释放连接的时候，是否对连接进行健康检查。
                //lastRecentUsed如果为true，表示获取连接时候从队列尾部获取。为false的时候从队列头部获取。建议使用FIFO，否则可能会导致一直获取一个连接。
                log.debug("new pool HostName:{} Port:{} MaxConnections:{} NewOnAcquireTimeoutAction:{}", inetSocketAddress.getHostName(), inetSocketAddress.getPort(), maxConnections, newOnAcquireTimeoutAction);
                return new FixedChannelPool(bootstrap.remoteAddress(inetSocketAddress), new ClientChannelPoolHandler(), ChannelHealthChecker.ACTIVE, newOnAcquireTimeoutAction ? FixedChannelPool.AcquireTimeoutAction.NEW : FixedChannelPool.AcquireTimeoutAction.FAIL, 200, maxConnections, RpcConstant.MAX_PENDING_ACQUIRES, true, false);
            }
        };
    }

    @Override
    public byte[] sendRequest(byte[] data, Service service) throws InterruptedException {
        return null;
    }

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest, Service service, RpcProtocolEnum messageProtocol, RpcCompressEnum compresser) {
        String address = service.getAddress();
        String[] addressInfo = address.split(":");
        final String serverAddress = addressInfo[0];
        final String serverPort = addressInfo[1];
        InetSocketAddress remoteaddress = InetSocketAddress.createUnresolved(serverAddress, Integer.parseInt(serverPort));
        try {
            return rpcRequest.isAsync() ? sendAsyncRequest(remoteaddress, rpcRequest, messageProtocol, compresser) : sendSyncRequest(remoteaddress, rpcRequest, messageProtocol, compresser);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RpcException("sendRequest error");
        }
    }

    public RpcResponse sendSyncRequest(InetSocketAddress socketAddress, RpcRequest request, RpcProtocolEnum messageProtocol, RpcCompressEnum compresser) throws ExecutionException, InterruptedException, TimeoutException {
        RpcResponse response;
        RpcFuture<RpcResponse> responseFuture = new RpcFuture<>();
        SendBaseHandler.requestMap.put(request.getRequestId(), responseFuture);
        SimpleChannelPool channelPool = connectedServerNodesPool.get(socketAddress);
        Channel channel = channelPool.acquire().get(RpcConstant.POOL_CHANNEL_WAIT_TIME, TimeUnit.SECONDS);
        try {
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setProtocol(messageProtocol);
            rpcMessage.setCompress(compresser);
            rpcMessage.setType(RpcConstant.REQUEST_TYPE);
            rpcMessage.setData(request);
            channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            //waiting
            response = responseFuture.get(RpcConstant.RESPONSE_WAIT_TIME, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RpcException(e.getMessage());
        } finally {
            channelPool.release(channel);
            log.debug("release address:{} channel:{}", socketAddress, channel.id());
            log.debug("requestMap remove:{}", request.getRequestId());
            SendBaseHandler.requestMap.remove(request.getRequestId());
        }
        return response;
    }

    private RpcResponse sendAsyncRequest(InetSocketAddress socketAddress, RpcRequest request, RpcProtocolEnum messageProtocol, RpcCompressEnum compresser) throws ExecutionException, InterruptedException, TimeoutException {
        RpcResponse response = new RpcResponse();
        RpcFuture<RpcResponse> responseFuture = new RpcFuture<>();
        Object asyncContext = ClientProxyFactory.getAsyncContext();
        AsyncReceiveHandler asyncReceiveHandler = ClientProxyFactory.getAsyncReceiveHandler();
        responseFuture.setAsyncReceiveHandler(asyncReceiveHandler);
        responseFuture.setAsyncContext(asyncContext);
        SendBaseHandler.requestMap.put(request.getRequestId(), responseFuture);
        SimpleChannelPool channelPool = connectedServerNodesPool.get(socketAddress);
        Channel channel = channelPool.acquire().get(RpcConstant.POOL_CHANNEL_WAIT_TIME, TimeUnit.MILLISECONDS);
        try {
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setProtocol(messageProtocol);
            rpcMessage.setCompress(compresser);
            rpcMessage.setType(RpcConstant.REQUEST_TYPE);
            rpcMessage.setData(request);
            channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            response.setAsync(request.isAsync());
            response.setRequestId(request.getRequestId());
            response.setReturnValue(null);
            response.setRpcStatus(RpcStatusEnum.SUCCESS);
        } catch (Exception e) {
            channelPool.release(channel);
            log.debug("release address:{} channel:{}", socketAddress, channel.id());
            SendBaseHandler.requestMap.remove(request.getRequestId());
            log.debug("requestMap remove:{}", request.getRequestId());
            throw new RpcException(e.getMessage());
        }
        return response;
    }
}