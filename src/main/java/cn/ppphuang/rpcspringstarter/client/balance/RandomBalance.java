package cn.ppphuang.rpcspringstarter.client.balance;

import cn.ppphuang.rpcspringstarter.annotation.LoadBalanceAno;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.model.Service;

import java.util.List;
import java.util.Random;

/**
 * 随机负载
 *
 * @Author: ppphuang
 * @Create: 2021/9/12
 */
@LoadBalanceAno(RpcConstant.BALANCE_RANDOM)
public class RandomBalance implements LoadBalance {

    private static Random random = new Random();

    @Override
    public Service selectOne(List<Service> services) {
        return services.get(random.nextInt(services.size()));
    }
}
