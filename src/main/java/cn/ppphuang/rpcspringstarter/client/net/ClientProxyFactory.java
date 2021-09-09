package cn.ppphuang.rpcspringstarter.client.net;

import cn.ppphuang.rpcspringstarter.client.cache.ServerDiscoveryCache;
import cn.ppphuang.rpcspringstarter.client.discovery.ServiceDiscoverer;
import cn.ppphuang.rpcspringstarter.common.model.Service;
import cn.ppphuang.rpcspringstarter.common.protocol.MessageProtocol;
import cn.ppphuang.rpcspringstarter.exception.RpcException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户端代理工厂：用于创建远程服务代理类
 * 封装编组请求、请求发送、编组响应等操作
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
public class ClientProxyFactory {
    private ServiceDiscoverer serviceDiscoverer;

    private NetClient netClient;

    private Map<String, MessageProtocol> supportMessageProtocols;

    private Map<Class<?>, Object> objectCache = new HashMap<>();

//    private LoadBalance loadBalance;

    public <T> T getProxy(Class<T> clazz) {
        return (T) objectCache.computeIfAbsent(clazz, clz -> Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, new ClientInvocationHandler(clz)));
    }

    private class ClientInvocationHandler implements InvocationHandler {

        private Class<?> clazz;

        public ClientInvocationHandler(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("toString")) {
                return proxy.toString();
            }

            if (method.getName().equals("hashCode")) {
                return 0;
            }

            //1. 获得服务信息
            String serviceName = clazz.getName();
            List<Service> serviceList = getServiceList(serviceName);
//            todo
            Service service = serviceList.get(0);
            //2. 构建request对象
            return null;
        }
    }

    public List<Service> getServiceList(String serviceName) {
        List<Service> services;
        synchronized (serviceName) {
            if (ServerDiscoveryCache.isEmpty(serviceName)) {
                services = serviceDiscoverer.getServices(serviceName);
                if (services == null || services.isEmpty()) {
                    throw new RpcException("NO provider available!");
                }
                ServerDiscoveryCache.put(serviceName, services);
            } else {
                services = ServerDiscoveryCache.get(serviceName);
            }
        }
        return services;
    }
}
