package cn.ppphuang.rpcspringstarter.config;

import cn.ppphuang.rpcspringstarter.annotation.LoadBalanceAno;
import cn.ppphuang.rpcspringstarter.annotation.MessageProtocolAno;
import cn.ppphuang.rpcspringstarter.annotation.ServerProxyAno;
import cn.ppphuang.rpcspringstarter.client.balance.LoadBalance;
import cn.ppphuang.rpcspringstarter.client.discovery.ZookeeperServerDiscovery;
import cn.ppphuang.rpcspringstarter.client.net.ClientProxyFactory;
import cn.ppphuang.rpcspringstarter.client.net.NettyNetClient;
import cn.ppphuang.rpcspringstarter.common.protocol.MessageProtocol;
import cn.ppphuang.rpcspringstarter.exception.RpcException;
import cn.ppphuang.rpcspringstarter.properties.RpcConfig;
import cn.ppphuang.rpcspringstarter.server.*;
import cn.ppphuang.rpcspringstarter.server.handler.RequestBaseHandler;
import cn.ppphuang.rpcspringstarter.server.register.DefaultRpcProcessor;
import cn.ppphuang.rpcspringstarter.server.register.ServerRegister;
import cn.ppphuang.rpcspringstarter.server.register.ZookeeperServerRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.HashMap;
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
    public RequestBaseHandler requestBaseHandler(@Autowired ServerRegister serverRegister, @Autowired RpcConfig rpcConfig) {
        RequestBaseHandler requestHandler = getRequestHandler(rpcConfig.getServerProxyType());
        requestHandler.setServerRegister(serverRegister);
        requestHandler.setProtocol(getMessageProtocol(rpcConfig.getProtocol()));
        return requestHandler;
    }

    @Bean
    public RpcServer rpcServer(@Autowired RequestBaseHandler requestBaseHandler, @Autowired RpcConfig rpcConfig) {
        return new NettyRpcServer(rpcConfig.getServerPort(), rpcConfig.getProtocol(), requestBaseHandler);
    }

    @Bean
    public ClientProxyFactory proxyFactory(@Autowired RpcConfig rpcConfig) {
        ClientProxyFactory clientProxyFactory = new ClientProxyFactory();
        //setServiceDiscoverer
        clientProxyFactory.setServiceDiscoverer(new ZookeeperServerDiscovery(rpcConfig.getRegisterAddress()));
        //setSupportMessageProtocols
        Map<String, MessageProtocol> supportMessageProtocol = buildSupportMessageProtocol();
        clientProxyFactory.setSupportMessageProtocols(supportMessageProtocol);
        LoadBalance loadBalance = getLoadBalance(rpcConfig.getLoadBalance());
        clientProxyFactory.setLoadBalance(loadBalance);
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

    private LoadBalance getLoadBalance(String name) {
        ServiceLoader<LoadBalance> loadBalances = ServiceLoader.load(LoadBalance.class);
        for (LoadBalance loadBalance : loadBalances) {
            LoadBalanceAno lb = loadBalance.getClass().getAnnotation(LoadBalanceAno.class);
            Assert.notNull(lb, "load balance name can not be empty!");
            if (name.equals(lb.value())) {
                return loadBalance;
            }
        }
        throw new RpcException("invalid load balance config");
    }

    private RequestBaseHandler getRequestHandler(String name) {
        ServiceLoader<RequestBaseHandler> handlers = ServiceLoader.load(RequestBaseHandler.class);
        for (RequestBaseHandler handler : handlers) {
            ServerProxyAno rh = handler.getClass().getAnnotation(ServerProxyAno.class);
            Assert.notNull(rh, "load server proxy type can not be empty!");
            if (name.equals(rh.value())) {
                return handler;
            }
        }
        throw new RpcException("invalid server proxy config");
    }
}
