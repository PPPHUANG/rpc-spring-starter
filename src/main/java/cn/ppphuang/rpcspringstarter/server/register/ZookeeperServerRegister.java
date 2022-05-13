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
import java.util.ArrayList;

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

    private static ArrayList<String> ExportedServiceURI = new ArrayList<>();

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
        //父类serviceMap中的同一个service可能有不同版本，不同版本的发布到注册中心的URI一样，所以用ExportedServiceURI记录的已发布的URI来避免重复删除
        ExportedServiceURI.forEach(this::removeService);
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
        //同一个service可能有多个版本的实现，所以可能已经被注册到注册中心，这样就不需要注册了
        if (!zkClient.exists(uriPath)) {
            //创建一个临时节点，会话失效即被清理
            zkClient.createEphemeral(uriPath);
            //发布的URI记录到map里，方便关机取消注册
            ExportedServiceURI.add(uriPath);
            log.debug("service :{} exported zk", serviceResource);
        }
    }

    /**
     * 从zk中移除服务
     *
     * @param serviceUri
     */
    public void removeService(String serviceUri) {
        boolean delete = zkClient.delete(serviceUri);
        log.debug("remove serviceURI :{} res :{}", serviceUri, delete);
    }
}
