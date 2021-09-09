package cn.ppphuang.rpcspringstarter.client.net;

import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.common.model.Service;
import cn.ppphuang.rpcspringstarter.common.protocol.MessageProtocol;

/**
 * 客户端网络接口
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
public interface NetClient {
    /**
     * 请求接口 返回字节数组
     * @param data
     * @param service
     * @return
     * @throws InterruptedException
     */
    byte[] sendRequest(byte[] data, Service service) throws InterruptedException;

    /**
     *请求接口 返回RpcResponse
     * @param rpcResponse
     * @param service
     * @param messageProtocol
     * @return
     */
    RpcResponse sendRequest(RpcRequest rpcRequest, Service service, MessageProtocol messageProtocol);
}
