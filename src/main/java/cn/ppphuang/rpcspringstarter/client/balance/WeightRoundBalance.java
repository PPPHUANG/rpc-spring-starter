package cn.ppphuang.rpcspringstarter.client.balance;

import cn.ppphuang.rpcspringstarter.annotation.LoadBalanceAno;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.model.Service;

import java.util.List;

/**
 * 加权轮询负载
 *
 * @Author: ppphuang
 * @Create: 2021/9/13
 */
@LoadBalanceAno(RpcConstant.BALANCE_WEIGHT_ROUND)
public class WeightRoundBalance implements LoadBalance {

    private static int index;

    @Override
    public Service selectOne(List<Service> services) {
        int allWeight = services.stream().mapToInt(Service::getWeight).sum();
        int num = (index++) % allWeight;
        for (Service service : services) {
            if (service.getWeight() > num) {
                return service;
            }
        }
        return null;
    }
}
