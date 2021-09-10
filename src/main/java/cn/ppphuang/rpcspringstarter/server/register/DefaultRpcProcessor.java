package cn.ppphuang.rpcspringstarter.server.register;

import cn.ppphuang.rpcspringstarter.annotation.Service;
import cn.ppphuang.rpcspringstarter.client.net.ClientProxyFactory;
import cn.ppphuang.rpcspringstarter.server.RpcServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;

/**
 * RPC处理者，服务启动注册，自动注入Service
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
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
    }

    private void startServer(ApplicationContext context) {
        Map<String, Object> beans = context.getBeansWithAnnotation(Service.class);
        if (beans.size() > 0) {
            boolean startServerFlag = true;
            for (Object obj : beans.values()) {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
