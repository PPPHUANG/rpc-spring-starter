package cn.ppphuang.rpcspringstarter.server;

import cn.ppphuang.rpcspringstarter.server.handler.RequestBaseHandler;

/**
 * 服务抽象类
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
public abstract class RpcServer {

    /**
     * 服务端口
     */
    protected int port;

    /**
     * 服务协议
     */
    protected String protocol;

    /**
     * 请求handler
     */
    protected RequestBaseHandler requestHandler;

    /**
     * 开启
     */
    public abstract void start();

    /**
     * 停止
     */
    public abstract void stop();


    public RpcServer(int port, String protocol, RequestBaseHandler requestHandler) {
        this.port = port;
        this.protocol = protocol;
        this.requestHandler = requestHandler;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public RequestBaseHandler getRequestHandler() {
        return requestHandler;
    }

    public void setRequestHandler(RequestBaseHandler requestHandler) {
        this.requestHandler = requestHandler;
    }
}
