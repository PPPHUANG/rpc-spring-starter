package cn.ppphuang.rpcspringstarter.client.net;

import cn.ppphuang.rpcspringstarter.common.constants.RpcCompressEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcProtocolEnum;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.common.model.Service;

/**
 * 客户端网络接口
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
public interface NetClient {
    /**
     * 请求接口 返回字节数组
     *
     * @param data
     * @param service
     * @return
     * @throws InterruptedException
     */
    byte[] sendRequest(byte[] data, Service service) throws InterruptedException;

    /**
     * 请求接口 返回RpcResponse
     *
     * @param rpcRequest
     * @param service
     * @param messageProtocol
     * @param compresser
     * @return
     */
    RpcResponse sendRequest(RpcRequest rpcRequest, Service service, RpcProtocolEnum messageProtocol, RpcCompressEnum compresser);
}
