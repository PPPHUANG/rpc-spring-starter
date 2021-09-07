package cn.ppphuang.rpcspringstarter.common.model;

/**
 * @Author: ppphuang
 * @Create: 2021/9/7
 */
public class Service {
    /**
     * 服务名称
     */
    private String name;

    /**
     * 服务协议
     */
    private String protocol;

    /**
     * 服务地址, ip:port
     */
    private String address;

    /**
     * 权重，越小优先级越低
     */
    private Integer weight;
}
