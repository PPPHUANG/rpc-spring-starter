package cn.ppphuang.rpcspringstarter.client.async;

import java.util.concurrent.*;
import java.util.function.Function;

/**
 * java8 CompletableFuture 超时处理
 *
 * @Author: ppphuang
 * @Create: 2022/5/22
 */
public class TimeoutUtils {
    private static final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(5);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(scheduledExecutor::shutdownNow));
    }

    public static <T> CompletableFuture<T> timeout(CompletableFuture<T> cf, long timeout, TimeUnit unit) {
        CompletableFuture<T> result = new CompletableFuture<>();
        scheduledExecutor.schedule(() -> result.completeExceptionally(new TimeoutException()), timeout, unit);
        return cf.applyToEither(result, Function.identity());
    }
}
