package cn.ppphuang.rpcspringstarter.server.register;

import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.model.Service;
import cn.ppphuang.rpcspringstarter.common.serializer.ZookeeperSerializer;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * zk服务注册器，提供服务注册、发现能力
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
@Slf4j
public class ZookeeperServerRegister extends DefaultServerRegisrer {

    private static final Gson GSON = new Gson();

    private ZkClient zkClient;

    public ZookeeperServerRegister(String zkAddress, Integer port, String protocol, String compress, Integer weight) {
        zkClient = new ZkClient(zkAddress);
        zkClient.setZkSerializer(new ZookeeperSerializer());
        this.port = port;
        this.protocol = protocol;
        this.compress = compress;
        this.weight = weight;
    }

    @Override
    public void register(ServiceObject so) throws Exception {
        super.register(so);
        Service service = getService(so);
        exportService(service);
    }

    private Service getService(ServiceObject so) throws UnknownHostException {
        Service service = new Service();
        String host = InetAddress.getLocalHost().getHostAddress();
        String address = host + ":" + port;
        service.setAddress(address);
        service.setName(so.getName());
        service.setProtocol(protocol);
        service.setWeight(weight);
        service.setCompress(compress);
        return service;
    }

    @Override
    public void remove() {
        Map<String, ServiceObject> allServiceObject = getAllServiceObject();
        if (!allServiceObject.isEmpty()) {
            allServiceObject.forEach((name, service) -> {
                try {
                    removeService(getService(service));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * 服务注册到zk
     *
     * @param serviceResource
     */
    public void exportService(Service serviceResource) {
        String name = serviceResource.getName();
        String uri = GSON.toJson(serviceResource);
        try {
            uri = URLEncoder.encode(uri, RpcConstant.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String servicePath = RpcConstant.ZK_SERVICE_PATH + RpcConstant.ZK_PATH_DELIMITER + name + "/service";
        if (!zkClient.exists(servicePath)) {
            //没有就注册
            zkClient.createPersistent(servicePath, true);
        }
        String uriPath = servicePath + RpcConstant.ZK_PATH_DELIMITER + uri;
        if (zkClient.exists(uriPath)) {
            //删除之前的节点
            zkClient.delete(uriPath);
        }
        //创建一个临时节点，会话失效即被清理
        zkClient.createEphemeral(uriPath);
    }

    /**
     * 从zk中移除服务
     *
     * @param serviceResource
     */
    public void removeService(Service serviceResource) {
        String name = serviceResource.getName();
        String uri = GSON.toJson(serviceResource);
        try {
            uri = URLEncoder.encode(uri, RpcConstant.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String servicePath = RpcConstant.ZK_SERVICE_PATH + RpcConstant.ZK_PATH_DELIMITER + name + "/service";
        String uriPath = servicePath + RpcConstant.ZK_PATH_DELIMITER + uri;
        boolean delete = zkClient.delete(uriPath);
        log.info("remove service :{} res :{}", serviceResource, delete);
    }
}
