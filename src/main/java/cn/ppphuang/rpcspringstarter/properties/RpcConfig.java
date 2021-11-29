package cn.ppphuang.rpcspringstarter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rpc 配置
 *
 * @Author: ppphuang
 * @Create: 2021/9/11
 */
@ConfigurationProperties(prefix = "hp.rpc")
public class RpcConfig {
    /**
     * 注册中心地址
     */
    private String registerAddress = "127.0.0.1:2128";

    /**
     * 服务暴露端口
     */
    private Integer serverPort = 9999;

    /**
     * 服务协议
     */
    private String protocol = "kryo";

    /**
     * 负载均衡算法
     */
    private String loadBalance = "random";

    /**
     * 权重
     */
    private Integer weight = 1;

    /**
     * 服务代理类型 reflect： 反射调用 javassist： 字节码生成代理类调用
     */
    private String serverProxyType = "javassist";

    public String getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(String registerAddress) {
        this.registerAddress = registerAddress;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getServerProxyType() {
        return serverProxyType;
    }

    public void setServerProxyType(String serverProxyType) {
        this.serverProxyType = serverProxyType;
    }
}
