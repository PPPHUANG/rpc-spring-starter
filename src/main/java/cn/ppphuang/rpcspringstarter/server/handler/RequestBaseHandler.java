package cn.ppphuang.rpcspringstarter.server.handler;

import cn.ppphuang.rpcspringstarter.common.constants.RpcCompressEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcProtocolEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcStatusEnum;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.server.register.ServerRegister;
import cn.ppphuang.rpcspringstarter.server.register.ServiceObject;
import lombok.extern.slf4j.Slf4j;

/**
 * 请求处理handler
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
@Slf4j
public abstract class RequestBaseHandler {

    private RpcProtocolEnum protocol;

    private RpcCompressEnum compresser;

    private ServerRegister serverRegister;

    public RequestBaseHandler(RpcProtocolEnum protocol, ServerRegister serverRegister) {
        this.protocol = protocol;
        this.serverRegister = serverRegister;
    }

    public RequestBaseHandler() {
    }

    public RpcResponse handleRequest(RpcRequest request) throws Exception {
        //1. 解组消息
//        RpcRequest request = protocol.unmarshallingRequest(data);
        log.debug("the server receives encode message :{}", request);
        //2. 查找服务对象
        ServiceObject serviceObject = serverRegister.getServiceObject(request.getServiceName() + request.getGroup() + request.getVersion());
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
        return response;
    }

    /**
     * 具体代理调用
     *
     * @return RpcResponse
     */
    public abstract RpcResponse invoke(ServiceObject serviceObject, RpcRequest request) throws Exception;

    public RpcProtocolEnum getProtocol() {
        return protocol;
    }

    public void setProtocol(RpcProtocolEnum protocol) {
        this.protocol = protocol;
    }

    public ServerRegister getServerRegister() {
        return serverRegister;
    }

    public void setServerRegister(ServerRegister serverRegister) {
        this.serverRegister = serverRegister;
    }

    public RpcCompressEnum getCompresser() {
        return compresser;
    }

    public void setCompresser(RpcCompressEnum compresser) {
        this.compresser = compresser;
    }
}
