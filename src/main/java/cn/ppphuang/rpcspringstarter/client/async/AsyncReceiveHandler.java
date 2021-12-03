package cn.ppphuang.rpcspringstarter.client.async;

import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端异步请求回调
 *
 * @Author: ppphuang
 * @Create: 2021/12/01
 */
@Slf4j
public abstract class AsyncReceiveHandler {

    /**
     * 客户端接收到响应之后，调用此方法
     *
     * @throws Exception
     */
    public void success(Object context, RpcResponse response) throws Exception {
        AsyncCallBackExecutor.execute(() -> {
            log.debug("AsyncReceiveHandler success context:{} response:{}", context, response);
            //todo
            callBack(context, response.getReturnValue());
        });
    }

    /**
     * 重写此方法，添加异步接收到结果之后的业务逻辑
     *
     * @param context
     * @param result
     */
    public abstract void callBack(Object context, Object result);
}
