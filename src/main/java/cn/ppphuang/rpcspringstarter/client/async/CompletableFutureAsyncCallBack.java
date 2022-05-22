package cn.ppphuang.rpcspringstarter.client.async;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * CompletableFutureAsyncCallBack
 *
 * @Author: ppphuang
 * @Create: 2022/5/22
 */
@Slf4j
public class CompletableFutureAsyncCallBack extends AsyncReceiveHandler {
    private static volatile CompletableFutureAsyncCallBack INSTANCE;

    private CompletableFutureAsyncCallBack() {
    }

    public static CompletableFutureAsyncCallBack instance() {
        if (INSTANCE == null) {
            synchronized (CompletableFutureAsyncCallBack.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CompletableFutureAsyncCallBack();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void callBack(Object context, Object result) {
        if (!(context instanceof CompletableFuture)) {
            throw new IllegalStateException("the context must be CompletableFuture");
        }
        CompletableFuture future = (CompletableFuture) context;
        if (result instanceof Throwable) {
            future.completeExceptionally((Throwable) result);
            return;
        }
        log.info("result:{}", result);
        future.complete(result);
    }

    @Override
    public void onException(Object context, Object result, Exception e) {

    }
}
