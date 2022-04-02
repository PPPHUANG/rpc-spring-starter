package cn.ppphuang.rpcspringstarter.common.model;

import java.util.Objects;

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
     * 压缩协议
     */
    private String compress;

    /**
     * 服务地址, ip:port
     */
    private String address;

    /**
     * 权重，越小优先级越低
     */
    private Integer weight;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getCompress() {
        return compress;
    }

    public void setCompress(String compress) {
        this.compress = compress;
    }

    @Override
    public String toString() {
        return "Service{" +
                "name='" + name + '\'' +
                ", protocol='" + protocol + '\'' +
                ", compress='" + compress + '\'' +
                ", address='" + address + '\'' +
                ", weight=" + weight +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return name.equals(service.name) && protocol.equals(service.protocol) && compress.equals(service.compress) && address.equals(service.address) && weight.equals(service.weight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, protocol, compress, address, weight);
    }
}
