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

    @Override
    public String toString() {
        return "Service{" +
                "name='" + name + '\'' +
                ", protocol='" + protocol + '\'' +
                ", address='" + address + '\'' +
                ", weight=" + weight +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return Objects.equals(name, service.name) &&
                Objects.equals(protocol, service.protocol) &&
                Objects.equals(address, service.address) &&
                Objects.equals(weight, service.weight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, protocol, address, weight);
    }
}
