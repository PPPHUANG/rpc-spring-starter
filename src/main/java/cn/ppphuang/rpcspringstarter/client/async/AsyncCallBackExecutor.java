package cn.ppphuang.rpcspringstarter.client.async;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 客户端异步回调执行器
 *
 * @Author: ppphuang
 * @Create: 2021/12/03
 */
@Slf4j
public class AsyncCallBackExecutor {
    private static int workerCount = 4;

    private static class ThreadPoolExecutorHolder {
        static {
            log.info("call back executor work count is " + AsyncCallBackExecutor.workerCount);
        }

        private final static ThreadPoolExecutor callBackExecutor = new ThreadPoolExecutor(AsyncCallBackExecutor.workerCount, AsyncCallBackExecutor.workerCount, 2000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NameAsyncCallBackExecutorTreadFactory());
    }

    public static void execute(Runnable runnable) {
        ThreadPoolExecutorHolder.callBackExecutor.execute(runnable);
    }

    static class NameAsyncCallBackExecutorTreadFactory implements ThreadFactory {

        private final AtomicInteger mThreadNum = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "rpcClient-AsyncCallBackExecutor-" + mThreadNum.getAndIncrement());
        }
    }
}

