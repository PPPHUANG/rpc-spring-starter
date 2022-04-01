package cn.ppphuang.rpcspringstarter.common.model;

import cn.ppphuang.rpcspringstarter.common.constants.RpcStatusEnum;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应类
 * javassist代理类中不支持自动拆装箱，需要基本类型对应的构造方法
 * 自动拆装箱是JDK编译器语法糖，javassist是运行时，没有JDK编译过程
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
public class RpcResponse implements Serializable {
    private String requestId;

    private boolean async;

    private Map<String, String> headers = new HashMap<>();

    private Object returnValue;

    private Exception exception;

    private RpcStatusEnum rpcStatus;

    public RpcResponse() {
    }

    public RpcResponse(RpcStatusEnum rpcStatus) {
        this.rpcStatus = rpcStatus;
    }

    public RpcResponse(Object returnValue) {
        this.rpcStatus = RpcStatusEnum.SUCCESS;
        this.returnValue = returnValue;
    }

    public RpcResponse(int returnValue) {
        this.rpcStatus = RpcStatusEnum.SUCCESS;
        this.returnValue = returnValue;
    }

    public RpcResponse(long returnValue) {
        this.rpcStatus = RpcStatusEnum.SUCCESS;
        this.returnValue = returnValue;
    }

    public RpcResponse(short returnValue) {
        this.rpcStatus = RpcStatusEnum.SUCCESS;
        this.returnValue = returnValue;
    }

    public RpcResponse(double returnValue) {
        this.rpcStatus = RpcStatusEnum.SUCCESS;
        this.returnValue = returnValue;
    }

    public RpcResponse(float returnValue) {
        this.rpcStatus = RpcStatusEnum.SUCCESS;
        this.returnValue = returnValue;
    }

    public RpcResponse(byte returnValue) {
        this.rpcStatus = RpcStatusEnum.SUCCESS;
        this.returnValue = returnValue;
    }

    public RpcResponse(char returnValue) {
        this.rpcStatus = RpcStatusEnum.SUCCESS;
        this.returnValue = returnValue;
    }

    public RpcResponse(boolean returnValue) {
        this.rpcStatus = RpcStatusEnum.SUCCESS;
        this.returnValue = returnValue;
    }

    public RpcResponse(RpcStatusEnum rpcStatus, Object returnValue) {
        this.rpcStatus = rpcStatus;
        this.returnValue = returnValue;
    }

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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public RpcStatusEnum getRpcStatus() {
        return rpcStatus;
    }

    public void setRpcStatus(RpcStatusEnum rpcStatus) {
        this.rpcStatus = rpcStatus;
    }
}
