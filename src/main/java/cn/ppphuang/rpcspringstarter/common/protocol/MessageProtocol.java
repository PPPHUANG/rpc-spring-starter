package cn.ppphuang.rpcspringstarter.common.protocol;

import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;

/**
 * 消息协议
 * 编组请求、解组请求、编组响应、解组响应
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
public interface MessageProtocol {
    /**
     * 编组请求
     * @param request
     * @return
     * @throws Exception
     */
    byte[] marshallingRequest(RpcRequest request) throws Exception;

    /**
     * 解组请求
     * @param data
     * @return
     * @throws Exception
     */
    RpcRequest unmarshallingRequest(byte[] data) throws Exception;

    /**
     * 编组响应
     * @param response
     * @return
     * @throws Exception
     */
    byte[] marshallingResponse(RpcResponse response) throws Exception;

    /**
     * 解组响应
     * @param data
     * @return
     * @throws Exception
     */
    RpcResponse unmarshallingResponse(byte[] data) throws Exception;
}
