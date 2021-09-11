package cn.ppphuang.rpcspringstarter.config;

import cn.ppphuang.rpcspringstarter.annotation.MessageProtocolAno;
import cn.ppphuang.rpcspringstarter.client.discovery.ZookeeperServerDiscovery;
import cn.ppphuang.rpcspringstarter.client.net.ClientProxyFactory;
import cn.ppphuang.rpcspringstarter.client.net.NettyNetClient;
import cn.ppphuang.rpcspringstarter.common.protocol.MessageProtocol;
import cn.ppphuang.rpcspringstarter.exception.RpcException;
import cn.ppphuang.rpcspringstarter.properties.RpcConfig;
import cn.ppphuang.rpcspringstarter.server.NettyRpcServer;
import cn.ppphuang.rpcspringstarter.server.RequestHandler;
import cn.ppphuang.rpcspringstarter.server.RpcServer;
import cn.ppphuang.rpcspringstarter.server.register.DefaultRpcProcessor;
import cn.ppphuang.rpcspringstarter.server.register.ServerRegister;
import cn.ppphuang.rpcspringstarter.server.register.ZookeeperServerRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Spring 自动配置类
 *
 * @Author: ppphuang
 * @Create: 2021/9/11
 */
@Configuration
@EnableConfigurationProperties(RpcConfig.class)
public class RpcAutoConfiguration {
    @Bean
    public RpcConfig rpcConfig() {
        return new RpcConfig();
    }

    @Bean
    public ServerRegister serverRegister(@Autowired RpcConfig rpcConfig) {
        return new ZookeeperServerRegister(rpcConfig.getRegisterAddress(), rpcConfig.getServerPort(), rpcConfig.getProtocol(), rpcConfig.getWeight());
    }

    @Bean
    public RequestHandler requestHandler(@Autowired ServerRegister serverRegister, @Autowired RpcConfig rpcConfig) {
        return new RequestHandler(getMessageProtocol(rpcConfig.getProtocol()), serverRegister);
    }

    @Bean
    public RpcServer rpcServer(@Autowired RequestHandler requestHandler, @Autowired RpcConfig rpcConfig) {
        return new NettyRpcServer(rpcConfig.getServerPort(), rpcConfig.getProtocol(), requestHandler);
    }

    @Bean
    public ClientProxyFactory proxyFactory(@Autowired RpcConfig rpcConfig) {
        ClientProxyFactory clientProxyFactory = new ClientProxyFactory();
        //setServiceDiscoverer
        clientProxyFactory.setServiceDiscoverer(new ZookeeperServerDiscovery(rpcConfig.getRegisterAddress()));
        //setSupportMessageProtocols
        Map<String, MessageProtocol> supportMessageProtocol = buildSupportMessageProtocol();
        clientProxyFactory.setSupportMessageProtocols(supportMessageProtocol);
        // todo
        clientProxyFactory.setNetClient(new NettyNetClient());

        return clientProxyFactory;
    }

    @Bean
    public DefaultRpcProcessor rpcProcessor(@Autowired ClientProxyFactory clientProxyFactory,
                                            @Autowired ServerRegister serverRegister,
                                            @Autowired RpcServer rpcServer) {
        return new DefaultRpcProcessor(clientProxyFactory, serverRegister, rpcServer);
    }

    public MessageProtocol getMessageProtocol(String name) {
        ServiceLoader<MessageProtocol> loader = ServiceLoader.load(MessageProtocol.class);
        for (MessageProtocol messageProtocol : loader) {
            MessageProtocolAno annotation = messageProtocol.getClass().getAnnotation(MessageProtocolAno.class);
            Assert.notNull(annotation, "message protocol name can not be empty!");
            if (name.equals(annotation.value())) {
                return messageProtocol;
            }
        }
        throw new RpcException("invalid message protocol config!");
    }

    public Map<String, MessageProtocol> buildSupportMessageProtocol() {
        HashMap<String, MessageProtocol> supportMessageProtocol = new HashMap<>();
        ServiceLoader<MessageProtocol> loader = ServiceLoader.load(MessageProtocol.class);
        for (MessageProtocol messageProtocol : loader) {
            MessageProtocolAno annotation = messageProtocol.getClass().getAnnotation(MessageProtocolAno.class);
            Assert.notNull(annotation, "message protocol name can not be empty!");
            supportMessageProtocol.put(annotation.value(), messageProtocol);
        }
        return supportMessageProtocol;
    }
}
