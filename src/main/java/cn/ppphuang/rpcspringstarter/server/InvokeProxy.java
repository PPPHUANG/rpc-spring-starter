package cn.ppphuang.rpcspringstarter.server;

import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;

/**
 * 服务代理对象接口
 *
 * @Author: ppphuang
 * @Create: 2021/11/23
 */
public interface InvokeProxy {
    /**
     * invoke调用服务接口
     *
     * @param rpcRequest
     * @return
     * @throws Exception
     */
    RpcResponse invoke(RpcRequest rpcRequest) throws Exception;
}
