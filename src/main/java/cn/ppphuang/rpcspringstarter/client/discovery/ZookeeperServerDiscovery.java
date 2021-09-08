package cn.ppphuang.rpcspringstarter.client.discovery;

import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.model.Service;
import cn.ppphuang.rpcspringstarter.common.serializer.ZookeeperSerializer;
import com.google.gson.Gson;
import org.I0Itec.zkclient.ZkClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: ppphuang
 * @Create: 2021/9/8
 */
public class ZookeeperServerDiscovery implements ServiceDiscoverer {

    private final static Gson gson = new Gson();

    private ZkClient zkClient;

    public ZookeeperServerDiscovery(String zkAddress) {
        zkClient = new ZkClient(zkAddress);
        zkClient.setZkSerializer(new ZookeeperSerializer());
    }

    @Override
    public List<Service> getServices(String name) {
        String servicePath = RpcConstant.ZK_SERVICE_PATH + RpcConstant.ZK_PATH_DELIMITER + name + "/service";
        List<String> children = zkClient.getChildren(servicePath);
        return Optional.ofNullable(children).orElse(new ArrayList<>()).stream().map(str -> {
            String deCh = null;
            try {
                deCh = URLDecoder.decode(str, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return gson.fromJson(deCh, Service.class);
        }).collect(Collectors.toList());
    }

    public ZkClient getZkClient() {
        return zkClient;
    }
}
