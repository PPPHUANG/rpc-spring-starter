package cn.ppphuang.rpcspringstarter.server.handler;

import cn.ppphuang.rpcspringstarter.common.constants.RpcStatusEnum;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.common.protocol.MessageProtocol;
import cn.ppphuang.rpcspringstarter.server.register.ServerRegister;
import cn.ppphuang.rpcspringstarter.server.register.ServiceObject;

/**
 * 请求处理handler
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
public abstract class RequestBaseHandler {

    private MessageProtocol protocol;

    private ServerRegister serverRegister;

    public RequestBaseHandler(MessageProtocol protocol, ServerRegister serverRegister) {
        this.protocol = protocol;
        this.serverRegister = serverRegister;
    }

    public RequestBaseHandler() {
    }

    public byte[] handleRequest(byte[] data) throws Exception {
        //1. 解组消息
        RpcRequest request = protocol.unmarshallingRequest(data);
        //2. 查找服务对象
        ServiceObject serviceObject = serverRegister.getServiceObject(request.getServiceName());
        RpcResponse response = null;

        if (serviceObject == null) {
            response = new RpcResponse(RpcStatusEnum.NOT_FOUND);
        } else {
            try {
                //3. 反射调用对应的方法
                response = invoke(serviceObject, request);
            } catch (Exception e) {
                response = new RpcResponse(RpcStatusEnum.ERROR);
                response.setException(e);
            }
        }
        //响应
        response.setRequestId(request.getRequestId());
        response.setAsync(request.isAsync());
        return protocol.marshallingResponse(response);
    }

    /**
     * 具体代理调用
     *
     * @return RpcResponse
     */
    public abstract RpcResponse invoke(ServiceObject serviceObject, RpcRequest request) throws Exception;

    public MessageProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(MessageProtocol protocol) {
        this.protocol = protocol;
    }

    public ServerRegister getServerRegister() {
        return serverRegister;
    }

    public void setServerRegister(ServerRegister serverRegister) {
        this.serverRegister = serverRegister;
    }
}
