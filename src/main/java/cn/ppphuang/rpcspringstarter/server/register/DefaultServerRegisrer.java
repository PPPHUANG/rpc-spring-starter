package cn.ppphuang.rpcspringstarter.server.register;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认服务注册器
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
public abstract class DefaultServerRegisrer implements ServerRegister {

    private Map<String, ServiceObject> serviceMap = new HashMap<>();

    protected String protocol;

    protected String compress;

    protected Integer port;

    /**
     * 权重
     */
    protected Integer weight;

    @Override
    public void register(ServiceObject so) throws Exception {
        if (so == null) {
            throw new IllegalArgumentException("parameter cannot be empty");
        }
        serviceMap.put(so.getName(), so);
    }

    @Override
    public ServiceObject getServiceObject(String name) throws Exception {
        return serviceMap.get(name);
    }
}
