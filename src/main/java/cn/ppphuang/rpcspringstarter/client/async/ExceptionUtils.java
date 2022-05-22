package cn.ppphuang.rpcspringstarter.client.async;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * CompletableFuture异常工具类
 *
 * @Author: ppphuang
 * @Create: 2022/5/22
 */
public class ExceptionUtils {
    /**
     * 提取真正的异常
     */
    public static Throwable extractRealException(Throwable throwable) {
        if (throwable instanceof CompletionException || throwable instanceof ExecutionException) {
            if (throwable.getCause() != null) {
                return throwable.getCause();
            }
        }
        return throwable;
    }
}
