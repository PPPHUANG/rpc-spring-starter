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
    private String protocol = "java";

    /**
     * 负载均衡算法
     */
    private String loadBalance = "random";

    /**
     * 权重
     */
    private Integer weight = 1;

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
}
