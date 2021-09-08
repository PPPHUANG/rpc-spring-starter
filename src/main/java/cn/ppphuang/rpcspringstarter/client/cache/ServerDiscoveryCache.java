package cn.ppphuang.rpcspringstarter.client.cache;

import cn.ppphuang.rpcspringstarter.common.model.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author: ppphuang
 * @Create: 2021/9/8
 */
public class ServerDiscoveryCache {
    /**
     * key: serviceName
     */
    private static final Map<String, List<Service>> SERVER_MAP = new ConcurrentHashMap<>();

    /**
     * 客户端注入的远程服务service class
     */
    private static final List<Service> SERVER_CLASS_NAMES = new ArrayList<>();


    /**
     * 添加缓存
     *
     * @param serviceName
     * @param serviceList
     */
    public static void put(String serviceName, List<Service> serviceList) {
        SERVER_MAP.put(serviceName, serviceList);
    }

    /**
     * 去除指定缓存
     *
     * @param serviceName
     * @param service
     */
    public static void remove(String serviceName, Service service) {
        SERVER_MAP.computeIfPresent(serviceName, (key, value) ->
                value.stream().filter(s -> !s.toString().equals(service.toString())).collect(Collectors.toList())
        );
    }

    /**
     * 删除指定服务的缓存
     *
     * @param serviceName
     */
    public static void removeAll(String serviceName) {
        SERVER_MAP.remove(serviceName);
    }

    /**
     * 指定服务是否有缓存节点
     *
     * @param serviceName
     * @return
     */
    public static boolean isEmpty(String serviceName) {
        return SERVER_MAP.get(serviceName) == null || SERVER_MAP.get(serviceName).isEmpty();
    }

    /**
     * 获取指定服务的缓存节点
     *
     * @param serviceName
     * @return
     */
    public static List<Service> get(String serviceName) {
        return SERVER_MAP.get(serviceName);
    }
}
