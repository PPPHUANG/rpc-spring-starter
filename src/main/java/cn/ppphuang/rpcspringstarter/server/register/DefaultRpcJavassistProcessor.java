package cn.ppphuang.rpcspringstarter.server.register;

import cn.ppphuang.rpcspringstarter.annotation.RpcProcessorAno;
import cn.ppphuang.rpcspringstarter.annotation.RpcService;
import cn.ppphuang.rpcspringstarter.client.net.ClientProxyFactory;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.server.ProxyFactory;
import cn.ppphuang.rpcspringstarter.server.RpcServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * RPC处理者，服务启动注册，自动注入Service
 *
 * @Author: ppphuang
 * @Create: 2021/11/24
 */
@Slf4j
@RpcProcessorAno(RpcConstant.SERVER_PROXY_TYPE_JAVASSIST)
public class DefaultRpcJavassistProcessor extends DefaultRpcBaseProcessor {

    public DefaultRpcJavassistProcessor(ClientProxyFactory clientProxyFactory, ServerRegister serverRegister, RpcServer rpcServer) {
        super(clientProxyFactory,serverRegister,rpcServer);
    }

    public DefaultRpcJavassistProcessor() {
    }

    @Override
    protected void startServer(ApplicationContext context) {
        Map<String, Object> beans = context.getBeansWithAnnotation(RpcService.class);
        //判定
        if (beans.size() > 0) {
            boolean startServerFlag = true;
            for (Map.Entry<String, Object> entry : beans.entrySet()) {
                try {
                    String beanName = entry.getKey();
                    Object obj  = entry.getValue();
                    Class<?> clazz = obj.getClass();
                    Class<?>[] interfaces = clazz.getInterfaces();
                    Method[] declaredMethods = clazz.getDeclaredMethods();
                    ServiceObject so = null;
                    /*
                     * 如果只实现了一个接口就用接口的className作为服务名
                     * 如果该类实现了多个接口，则使用注解里的value作为服务名
                     */
                    RpcService service = clazz.getAnnotation(RpcService.class);
                    if (interfaces.length != 1) {
                        String value = service.value();
                        if ("".equals(value)) {
                            startServerFlag = false;
                            throw new UnsupportedOperationException("The exposed interface is not specific with '" + obj.getClass().getName() + "'");
                        }
                        /*
                         * bean实现多个接口时，javassist代理类中生成的方法只按照注解指定的服务类来生成
                         */
                        declaredMethods = Class.forName(value).getDeclaredMethods();
                        Object proxy = ProxyFactory.makeProxy(value, beanName, declaredMethods);
                        so = new ServiceObject(value, Class.forName(value), proxy, service.group(), service.version());
                    } else {
                        Class<?> supperClass = interfaces[0];
                        Object proxy = ProxyFactory.makeProxy(supperClass.getName(), beanName, declaredMethods);
                        so = new ServiceObject(supperClass.getName(), supperClass, proxy, service.group(), service.version());
                    }
                    serverRegister.register(so);
                } catch (Exception e) {
                    log.error("rpc start exception :", e);
                }
            }

            if (startServerFlag) {
                rpcServer.start();
            }
        }
    }
}
