package cn.ppphuang.rpcspringstarter.common.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求类
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
public class RpcRequest implements Serializable {
    private String requestId;

    private boolean async;

    private String serviceName;

    private String method;

    private Map<String, String> headers = new HashMap<>();

    private Class<?>[] parametersTypes;

    private Object[] parameters;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Class<?>[] getParametersTypes() {
        return parametersTypes;
    }

    public void setParametersTypes(Class<?>[] parametersTypes) {
        this.parametersTypes = parametersTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
