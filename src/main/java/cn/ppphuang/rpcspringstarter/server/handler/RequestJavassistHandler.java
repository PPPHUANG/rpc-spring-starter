package cn.ppphuang.rpcspringstarter.server.handler;

import cn.ppphuang.rpcspringstarter.annotation.ServerProxyAno;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.common.protocol.MessageProtocol;
import cn.ppphuang.rpcspringstarter.server.InvokeProxy;
import cn.ppphuang.rpcspringstarter.server.register.ServerRegister;
import cn.ppphuang.rpcspringstarter.server.register.ServiceObject;


/**
 * 请求处理RequestJavassistHandler
 *
 * @Author: ppphuang
 * @Create: 2021/11/23
 */
@ServerProxyAno(RpcConstant.SERVER_PROXY_TYPE_JAVASSIST)
public class RequestJavassistHandler extends RequestBaseHandler {

    public RequestJavassistHandler(MessageProtocol protocol, ServerRegister serverRegister) {
        super(protocol, serverRegister);
    }

    public RequestJavassistHandler() {
    }

    @Override
    public RpcResponse invoke(ServiceObject serviceObject, RpcRequest request) throws Exception {
        InvokeProxy invokeProxy = (InvokeProxy) serviceObject.getObj();
        return invokeProxy.invoke(request);
    }
}
