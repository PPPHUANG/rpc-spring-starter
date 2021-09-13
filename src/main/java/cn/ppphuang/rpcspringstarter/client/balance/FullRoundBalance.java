package cn.ppphuang.rpcspringstarter.client.balance;

import cn.ppphuang.rpcspringstarter.annotation.LoadBalanceAno;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.model.Service;

import java.util.List;

/**
 * 轮询负载
 *
 * @Author: ppphuang
 * @Create: 2021/9/13
 */
@LoadBalanceAno(RpcConstant.BALANCE_FULL_ROUND)
public class FullRoundBalance implements LoadBalance {

    private int index;

    @Override
    public Service selectOne(List<Service> services) {
        if (index == services.size()) {
            index = 0;
        }
        return services.get(index++);
    }
}
