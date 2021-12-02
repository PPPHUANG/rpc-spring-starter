package cn.ppphuang.rpcspringstarter.client.async;

import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;

/**
 * 客户端异步请求回调
 *
 * @Author: ppphuang
 * @Create: 2021/12/01
 */
public abstract class AsyncReceiveHandler {

    /**
     * 客户端接收到请求之后，调用此方法
     *
     * @throws Exception
     */
    public void success(Object context, RpcResponse response) throws Exception {
        callBack(context, response);
    }

    /**
     * 重写此方法，添加异步接收到结果之后的业务逻辑
     *
     * @param context
     * @param result
     */
    public abstract void callBack(Object context, Object result);
}
