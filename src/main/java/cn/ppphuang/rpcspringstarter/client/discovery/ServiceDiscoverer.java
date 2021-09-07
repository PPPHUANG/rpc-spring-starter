package cn.ppphuang.rpcspringstarter.client.discovery;

import cn.ppphuang.rpcspringstarter.common.model.Service;

import java.util.List;

/**
 * 服务发现抽象类
 *
 * @Author: ppphuang
 * @Create: 2021/9/7
 */
public interface ServiceDiscoverer {
    List<Service> getServices(String name);
}
