package cn.ppphuang.rpcspringstarter.server.register;

import cn.ppphuang.rpcspringstarter.annotation.InjectService;
import cn.ppphuang.rpcspringstarter.annotation.Service;
import cn.ppphuang.rpcspringstarter.client.cache.ServerDiscoveryCache;
import cn.ppphuang.rpcspringstarter.client.discovery.ZkChildListenerImpl;
import cn.ppphuang.rpcspringstarter.client.discovery.ZookeeperServerDiscovery;
import cn.ppphuang.rpcspringstarter.client.net.ClientProxyFactory;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.server.RpcServer;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * RPC处理者，服务启动注册，自动注入Service
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
@Slf4j
public class DefaultRpcProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private ClientProxyFactory clientProxyFactory;

    private ServerRegister serverRegister;

    private RpcServer rpcServer;

    public DefaultRpcProcessor(ClientProxyFactory clientProxyFactory, ServerRegister serverRegister, RpcServer rpcServer) {
        this.clientProxyFactory = clientProxyFactory;
        this.serverRegister = serverRegister;
        this.rpcServer = rpcServer;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //Spring启动完毕会收到Event
        if (Objects.isNull(contextRefreshedEvent.getApplicationContext().getParent())) {
            ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
            startServer(applicationContext);
            injectService(applicationContext);
        }
    }

    private void injectService(ApplicationContext context) {
        String[] names = context.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = context.getBean(name);
            Class<?> clazz = bean.getClass();
            if (Objects.isNull(clazz)) {
                continue;
            }

            if (AopUtils.isCglibProxy(bean)) {
                //aop增强的类生成cglib类，需要Superclass才能获取定义的字段
                clazz = clazz.getSuperclass();
            } else if(AopUtils.isJdkDynamicProxy(bean)) {
                //动态代理类，可能也需要
                clazz = clazz.getSuperclass();
            }

            Field[] declaredFields = clazz.getDeclaredFields();
            //设置InjectService的代理类
            for (Field field : declaredFields) {
                InjectService injectService = field.getAnnotation(InjectService.class);
                if (injectService == null) {
                    continue;
                }
                Class<?> fieldClass = field.getType();
                Object object = context.getBean(name);
                field.setAccessible(true);
                try {
                    field.set(object, clientProxyFactory.getProxy(fieldClass));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                ServerDiscoveryCache.SERVER_CLASS_NAMES.add(fieldClass.getName());
            }
        }
        // 注册zk的子节点监听
        if (clientProxyFactory.getServiceDiscoverer() instanceof ZookeeperServerRegister) {
            ZookeeperServerDiscovery serverDiscovery = (ZookeeperServerDiscovery) clientProxyFactory.getServiceDiscoverer();
            ZkClient zkClient = serverDiscovery.getZkClient();
            ServerDiscoveryCache.SERVER_CLASS_NAMES.forEach(name -> {
                String servicePath = RpcConstant.ZK_SERVICE_PATH + RpcConstant.ZK_PATH_DELIMITER + name + "/service";
                zkClient.subscribeChildChanges(servicePath, new ZkChildListenerImpl());
            });
            log.info("subscribe service zk node successfully");
        }
    }

    private void startServer(ApplicationContext context) {
        Map<String, Object> beans = context.getBeansWithAnnotation(Service.class);
        if (beans.size() > 0) {
            boolean startServerFlag = true;
            for (Object obj : beans.values()) {
                try {
                    Class<?> clazz = obj.getClass();
                    Class<?>[] interfaces = clazz.getInterfaces();
                    ServiceObject so = null;
                    /**
                     * 如果只实现了一个接口就用接口的className作为服务名
                     * 如果该类实现了多个接口，则使用注解里的value作为服务名
                     */
                    if (interfaces.length != 1) {
                        Service service = clazz.getAnnotation(Service.class);
                        String value = service.value();
                        if ("".equals(value)) {
                            startServerFlag = false;
                            throw new UnsupportedOperationException("The exposed interface is not specific with '" + obj.getClass().getName() + "'");
                        }
                        so = new ServiceObject(value, Class.forName(value), obj);
                    } else {
                        Class<?> supperClass = interfaces[0];
                        so = new ServiceObject(supperClass.getName(), supperClass, obj);
                    }
                    serverRegister.register(so);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (startServerFlag) {
                rpcServer.start();
            }
        }
    }
}
