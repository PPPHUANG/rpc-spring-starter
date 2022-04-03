package cn.ppphuang.rpcspringstarter.client.net;

import cn.ppphuang.rpcspringstarter.client.async.AsyncReceiveHandler;
import cn.ppphuang.rpcspringstarter.client.balance.LoadBalance;
import cn.ppphuang.rpcspringstarter.client.cache.ServerDiscoveryCache;
import cn.ppphuang.rpcspringstarter.client.discovery.ServiceDiscoverer;
import cn.ppphuang.rpcspringstarter.common.compresser.Compresser;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.common.model.Service;
import cn.ppphuang.rpcspringstarter.common.protocol.MessageProtocol;
import cn.ppphuang.rpcspringstarter.exception.RpcException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private Map<String, Compresser> supportCompressers;

    private Map<String, Object> objectCache = new HashMap<>();

    private Map<String, Object> asyncObjectCache = new HashMap<>();

    private LoadBalance loadBalance;

    private static ThreadLocal<Object> localAsyncContext = new ThreadLocal<>();

    private static ThreadLocal<AsyncReceiveHandler> localAsyncReceiveHandler = new ThreadLocal<>();

    public <T> T getProxy(Class<T> clazz) {
        return getProxy(clazz, "", "", false);
    }

    public <T> T getProxy(Class<T> clazz, String group) {
        return getProxy(clazz, group, "", false);
    }

    public <T> T getProxy(Class<T> clazz, String group, String version) {
        return getProxy(clazz, group, version, false);
    }

    public <T> T getProxy(Class<T> clazz, String group, String version, boolean async) {
        if (async) {
            return (T) asyncObjectCache.computeIfAbsent(clazz.getName() + group + version, clz -> Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ClientInvocationHandler(clazz, group, version, async)));
        } else {
            return (T) objectCache.computeIfAbsent(clazz.getName() + group + version, clz -> Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ClientInvocationHandler(clazz, group, version, async)));
        }
    }

    private class ClientInvocationHandler implements InvocationHandler {

        private Class<?> clazz;

        private boolean async;

        private String group;

        private String version;

        public ClientInvocationHandler(Class<?> clazz, String group, String version, boolean async) {
            this.clazz = clazz;
            this.async = async;
            this.group = group;
            this.version = version;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("toString".equals(method.getName())) {
                return proxy.toString();
            }

            if ("hashCode".equals(method.getName())) {
                return 0;
            }

            //1. 获得服务信息
            String serviceName = clazz.getName();
            List<Service> serviceList = getServiceList(serviceName);
            Service service = loadBalance.selectOne(serviceList);
            //2. 构建request对象
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setRequestId(UUID.randomUUID().toString());
            rpcRequest.setAsync(async);
            rpcRequest.setServiceName(service.getName());
            rpcRequest.setMethod(method.getName());
            rpcRequest.setGroup(group);
            rpcRequest.setVersion(version);
            rpcRequest.setParameters(args);
            rpcRequest.setParametersTypes(method.getParameterTypes());
            //3. 协议编组
            MessageProtocol messageProtocol = supportMessageProtocols.get(service.getProtocol());
            Compresser compresser = supportCompressers.get(service.getCompress());
            RpcResponse response = netClient.sendRequest(rpcRequest, service, messageProtocol, compresser);
            if (response == null) {
                throw new RpcException("the response is null");
            }
            if (response.getException() != null) {
                throw response.getException();
            }
            return response.getReturnValue();
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

    public static void setLocalAsyncContextAndAsyncReceiveHandler(Object context, AsyncReceiveHandler asyncReceiveHandler) {
        localAsyncContext.set(context);
        localAsyncReceiveHandler.set(asyncReceiveHandler);
    }

    public static AsyncReceiveHandler getAsyncReceiveHandler() {
        return localAsyncReceiveHandler.get();
    }

    public static Object getAsyncContext() {
        return localAsyncContext.get();
    }

    public ServiceDiscoverer getServiceDiscoverer() {
        return serviceDiscoverer;
    }

    public void setServiceDiscoverer(ServiceDiscoverer serviceDiscoverer) {
        this.serviceDiscoverer = serviceDiscoverer;
    }

    public NetClient getNetClient() {
        return netClient;
    }

    public void setNetClient(NetClient netClient) {
        this.netClient = netClient;
    }

    public Map<String, MessageProtocol> getSupportMessageProtocols() {
        return supportMessageProtocols;
    }

    public void setSupportMessageProtocols(Map<String, MessageProtocol> supportMessageProtocols) {
        this.supportMessageProtocols = supportMessageProtocols;
    }

    public Map<String, Compresser> getSupportCompressers() {
        return supportCompressers;
    }

    public void setSupportCompressers(Map<String, Compresser> supportCompressers) {
        this.supportCompressers = supportCompressers;
    }

    public Map<String, Object> getObjectCache() {
        return objectCache;
    }

    public void setObjectCache(Map<String, Object> objectCache) {
        this.objectCache = objectCache;
    }

    public LoadBalance getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }
}
