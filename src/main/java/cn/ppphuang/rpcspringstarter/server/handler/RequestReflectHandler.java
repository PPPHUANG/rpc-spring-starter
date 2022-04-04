package cn.ppphuang.rpcspringstarter.server.handler;

import cn.ppphuang.rpcspringstarter.annotation.ServerProxyAno;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.constants.RpcProtocolEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcStatusEnum;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.server.register.ServerRegister;
import cn.ppphuang.rpcspringstarter.server.register.ServiceObject;

import java.lang.reflect.Method;

/**
 * 请求处理RequestReflectHandler
 *
 * @Author: ppphuang
 * @Create: 2021/11/23
 */
@ServerProxyAno(RpcConstant.SERVER_PROXY_TYPE_REFLECT)
public class RequestReflectHandler extends RequestBaseHandler {

    public RequestReflectHandler(RpcProtocolEnum protocol, ServerRegister serverRegister) {
        super(protocol, serverRegister);
    }

    public RequestReflectHandler() {
    }

    @Override
    public RpcResponse invoke(ServiceObject serviceObject, RpcRequest request) throws Exception {
        Method method = serviceObject.getClazz().getMethod(request.getMethod(), request.getParametersTypes());
        Object value = method.invoke(serviceObject.getObj(), request.getParameters());
        RpcResponse response = new RpcResponse(RpcStatusEnum.SUCCESS);
        response.setReturnValue(value);
        return response;
    }
}
