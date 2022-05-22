package cn.ppphuang.rpcspringstarter.client.async;

import java.util.function.BiConsumer;

/**
 * 异常记录实现类
 *
 * @Author: ppphuang
 * @Create: 2022/5/22
 */
public class LogErrorAction<R> extends AbstractLogAction<R> implements BiConsumer<R, Throwable> {
    public LogErrorAction(String methodName, Object... args) {
        super(methodName, args);
    }

    @Override
    public void accept(R result, Throwable throwable) {
        logResult(result, throwable);
    }
}
