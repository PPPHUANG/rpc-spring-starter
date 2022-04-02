package cn.ppphuang.rpcspringstarter.server.register;

import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.model.Service;
import cn.ppphuang.rpcspringstarter.common.serializer.ZookeeperSerializer;
import com.google.gson.Gson;
import org.I0Itec.zkclient.ZkClient;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;

/**
 * zk服务注册器，提供服务注册、发现能力
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
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
        Service service = new Service();
        String host = InetAddress.getLocalHost().getHostAddress();
        String address = host + ":" + port;
        service.setAddress(address);
        service.setName(so.getName());
        service.setProtocol(protocol);
        service.setWeight(weight);
        service.setCompress(compress);
        exportService(service);
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
}
