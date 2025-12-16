package cn.ppphuang.rpcspringstarter.client.async;

import cn.ppphuang.rpcspringstarter.client.net.ClientProxyFactory;

import java.util.function.Function;

/**
 * 包装CompletableFuture异步调用方式的代理类
 *
 * @Author: ppphuang
 * @Create: 2022/5/22
 */
public class AsyncExecutor<C> {

    private C client;

    public AsyncExecutor(ClientProxyFactory clientProxyFactory, Class<C> clazz, String group, String version) {
        this.client = clientProxyFactory.getProxy(clazz, group, version, true);
    }

    public <R> RpcCompletableFuture<R> async(Function<C, R> function) {
        RpcCompletableFuture<R> future = new RpcCompletableFuture<>();
        ClientProxyFactory.setLocalAsyncContextAndAsyncReceiveHandler(future, CompletableFutureAsyncCallBack.instance());
        try {
            function.apply(client);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }
}
