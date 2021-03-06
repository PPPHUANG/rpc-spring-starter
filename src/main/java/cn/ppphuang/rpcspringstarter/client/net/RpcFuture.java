package cn.ppphuang.rpcspringstarter.client.net;

import cn.ppphuang.rpcspringstarter.client.async.AsyncReceiveHandler;

import java.util.concurrent.*;

/**
 * 处理回调
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
public class RpcFuture<T> implements Future<T> {

    private T response;

    private AsyncReceiveHandler asyncReceiveHandler;

    private Object asyncContext;

    private CountDownLatch countDownLatch;

    private long beginTime = System.currentTimeMillis();

    public RpcFuture() {
        this.countDownLatch = new CountDownLatch(1);
    }

    public RpcFuture(AsyncReceiveHandler asyncReceiveHandler, Object asyncContext) {
        this.asyncReceiveHandler = asyncReceiveHandler;
        this.asyncContext = asyncContext;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return response != null;
    }

    /**
     * 阻塞获取结果
     *
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Override
    public T get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return response;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (countDownLatch.await(timeout, unit)) {
            return response;
        }
        return null;
    }

    public void setResponse(T response) {
        this.response = response;
        countDownLatch.countDown();
    }

    public long getBeginTime() {
        return beginTime;
    }

    public AsyncReceiveHandler getAsyncReceiveHandler() {
        return asyncReceiveHandler;
    }

    public void setAsyncReceiveHandler(AsyncReceiveHandler asyncReceiveHandler) {
        this.asyncReceiveHandler = asyncReceiveHandler;
    }

    public Object getAsyncContext() {
        return asyncContext;
    }

    public void setAsyncContext(Object asyncContext) {
        this.asyncContext = asyncContext;
    }
}
