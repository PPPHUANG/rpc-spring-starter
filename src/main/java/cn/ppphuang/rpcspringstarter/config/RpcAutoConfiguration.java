package cn.ppphuang.rpcspringstarter.config;

import cn.ppphuang.rpcspringstarter.annotation.*;
import cn.ppphuang.rpcspringstarter.client.balance.LoadBalance;
import cn.ppphuang.rpcspringstarter.client.discovery.ZookeeperServerDiscovery;
import cn.ppphuang.rpcspringstarter.client.net.ClientProxyFactory;
import cn.ppphuang.rpcspringstarter.client.net.NettyNetClient;
import cn.ppphuang.rpcspringstarter.common.Extension.ExtensionLoader;
import cn.ppphuang.rpcspringstarter.common.compresser.Compresser;
import cn.ppphuang.rpcspringstarter.common.constants.RpcCompressEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcProtocolEnum;
import cn.ppphuang.rpcspringstarter.common.protocol.MessageProtocol;
import cn.ppphuang.rpcspringstarter.exception.RpcException;
import cn.ppphuang.rpcspringstarter.properties.RpcConfig;
import cn.ppphuang.rpcspringstarter.server.*;
import cn.ppphuang.rpcspringstarter.server.handler.RequestBaseHandler;
import cn.ppphuang.rpcspringstarter.server.register.DefaultRpcBaseProcessor;
import cn.ppphuang.rpcspringstarter.server.register.ServerRegister;
import cn.ppphuang.rpcspringstarter.server.register.ZookeeperServerRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
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
@ConditionalOnProperty(prefix = "hp.rpc", name = "enable", havingValue = "true", matchIfMissing = true)
public class RpcAutoConfiguration {

    @PostConstruct
    public void extensionLoader() {
        //setSupportMessageProtocols
        Map<String, MessageProtocol> supportMessageProtocol = buildSupportMessageProtocol();
        ExtensionLoader.setSupportMessageProtocols(supportMessageProtocol);
        //setSupportCompresser
        Map<String, Compresser> supportCompresser = buildSupportCompresser();
        ExtensionLoader.setSupportCompressers(supportCompresser);
    }

    @Bean
    public ServerRegister serverRegister(@Autowired RpcConfig rpcConfig) {
        return new ZookeeperServerRegister(rpcConfig.getRegisterAddress(), rpcConfig.getServerPort(), rpcConfig.getProtocol(), rpcConfig.isEnableCompress() ? rpcConfig.getCompress() : RpcCompressEnum.UNZIP.getCompress(), rpcConfig.getWeight());
    }

    @Bean
    public RequestBaseHandler requestBaseHandler(@Autowired ServerRegister serverRegister, @Autowired RpcConfig rpcConfig) {
        RequestBaseHandler requestHandler = getRequestHandler(rpcConfig.getServerProxyType());
        requestHandler.setServerRegister(serverRegister);
        requestHandler.setProtocol(RpcProtocolEnum.getProtocol(rpcConfig.getProtocol()));
        requestHandler.setCompresser(rpcConfig.isEnableCompress() ? RpcCompressEnum.getCompress(rpcConfig.getCompress()) : RpcCompressEnum.UNZIP);
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
        LoadBalance loadBalance = getLoadBalance(rpcConfig.getLoadBalance());
        clientProxyFactory.setLoadBalance(loadBalance);
        clientProxyFactory.setNetClient(new NettyNetClient());

        return clientProxyFactory;
    }

    @Bean
    public DefaultRpcBaseProcessor rpcProcessor(@Autowired ClientProxyFactory clientProxyFactory,
                                                @Autowired ServerRegister serverRegister,
                                                @Autowired RpcServer rpcServer,
                                                @Autowired RpcConfig rpcConfig) {
        DefaultRpcBaseProcessor defaultRpcProcessor = getDefaultRpcProcessor(rpcConfig.getServerProxyType());
        defaultRpcProcessor.setRpcServer(rpcServer);
        defaultRpcProcessor.setClientProxyFactory(clientProxyFactory);
        defaultRpcProcessor.setServerRegister(serverRegister);
        return defaultRpcProcessor;
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

    public static Map<String, MessageProtocol> buildSupportMessageProtocol() {
        HashMap<String, MessageProtocol> supportMessageProtocol = new HashMap<>();
        ServiceLoader<MessageProtocol> loader = ServiceLoader.load(MessageProtocol.class);
        for (MessageProtocol messageProtocol : loader) {
            MessageProtocolAno annotation = messageProtocol.getClass().getAnnotation(MessageProtocolAno.class);
            Assert.notNull(annotation, "message protocol name can not be empty!");
            supportMessageProtocol.put(annotation.value(), messageProtocol);
        }
        return supportMessageProtocol;
    }


    public Compresser getCompresser(String name) {
        ServiceLoader<Compresser> loader = ServiceLoader.load(Compresser.class);
        for (Compresser compresser : loader) {
            CompresserAno annotation = compresser.getClass().getAnnotation(CompresserAno.class);
            Assert.notNull(annotation, "compress name can not be empty!");
            if (name.equals(annotation.value())) {
                return compresser;
            }
        }
        throw new RpcException("invalid compress config!");
    }

    public Map<String, Compresser> buildSupportCompresser() {
        HashMap<String, Compresser> supportCompresser = new HashMap<>();
        ServiceLoader<Compresser> loader = ServiceLoader.load(Compresser.class);
        for (Compresser messageProtocol : loader) {
            CompresserAno compresser = messageProtocol.getClass().getAnnotation(CompresserAno.class);
            Assert.notNull(compresser, "compress name can not be empty!");
            supportCompresser.put(compresser.value(), messageProtocol);
        }
        return supportCompresser;
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

    private DefaultRpcBaseProcessor getDefaultRpcProcessor(String name) {
        ServiceLoader<DefaultRpcBaseProcessor> processors = ServiceLoader.load(DefaultRpcBaseProcessor.class);
        for (DefaultRpcBaseProcessor processor : processors) {
            RpcProcessorAno rp = processor.getClass().getAnnotation(RpcProcessorAno.class);
            Assert.notNull(rp, "load default rpc base processor can not be empty!");
            if (name.equals(rp.value())) {
                return processor;
            }
        }
        throw new RpcException("invalid default rpc base processor config");
    }
}
