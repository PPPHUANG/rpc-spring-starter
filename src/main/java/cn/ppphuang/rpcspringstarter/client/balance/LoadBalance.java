package cn.ppphuang.rpcspringstarter.client.balance;

import cn.ppphuang.rpcspringstarter.common.model.Service;

import java.util.List;

/**
 * 负载均衡接口
 *
 * @Author: ppphuang
 * @Create: 2021/9/12
 */
public interface LoadBalance {

    /**
     * 选择一个服务端
     *
     * @param services
     * @return
     */
    Service selectOne(List<Service> services);
}
