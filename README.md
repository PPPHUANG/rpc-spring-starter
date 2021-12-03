# rpc-spring-starter
## TODO
- [X] Netty通讯
- [x] 服务注册发现
- [x] 客户端负载均衡
- [x] Java、ProtoBuf、Kryo序列化
- [X] Netty增加编解码器
- [x] 服务端代理模式可配置 支持反射 字节码增强两种实现
- [x] 异步调用支持
- [ ] 调用鉴权
- [ ] 调用监控、告警
- [ ] 调用限流
- [ ] 调用降级、熔断
- [ ] 灰度支持

1. 本地install
```bash
mvn  clean install -DskipTests=true
```
2. 添加maven依赖到你的`SpringBoot`项目中
 ```xml
   <dependency>
        <groupId>cn.ppphuang</groupId>
        <artifactId>rpc-spring-starter</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
 ```

3. 默认配置项在`RpcConfig`类中，可以通过`application.properties`来覆盖需要修改的配置项

```properties
#注册中心地址
hp.rpc.register-address=127.0.0.1:2128
#服务暴露端口
hp.rpc.server-port=9999
#客户端随机负载均衡 默认：random，可选 random 随机 、round 轮询、 加权轮询 weightRound 、平滑加权轮询 smoothWeightRound
hp.rpc.load-balance=random
#序列化协议 默认：kryo, 可选 kryo 、 java 、 protobuf
hp.rpc.protocol=kryo
#服务代理类型 默认：javassist， 可选 reflect 反射调用、 javassist 字节码生成代理类调用
hp.rpc.server-proxy-type=javassist
#服务权重
hp.rpc.weight=1
```

## 服务端
提供远程方法并注入IOC
 ```java
import cn.ppphuang.rpcspringstarter.annotation.Service;
import cn.ppphuang.rpcspringstarter.service.HelloService;

@Service
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return name + "hello";
    }
}
 ```

**注意：** 这里的`@Service`是`rpcspringstarter`的注解。

## 客户端

### 同步调用

1. 使用`@InjectService`注解注入远程方法。

 ```java
import cn.ppphuang.rpcspringstarter.annotation.InjectService;
import cn.ppphuang.rpcspringstarter.service.HelloService;
import org.springframework.stereotype.Service;

@Service
public class TestService1 {
    @InjectService
    HelloService helloService;

    public String sayHai(String name) {
        return helloService.hello(name);
    }
}
 ```

**注意：** 这里的`@Service`是`Spring`的注解。

2. 活动获取代理对象。

```java
class RpcSpringStarterApplicationTests {

    @Autowired
    ClientProxyFactory clientProxyFactory;

    @Test
    void testSync() throws InterruptedException {
        HelloService proxy = clientProxyFactory.getProxy(HelloService.class);
        String ppphuang = proxy.hello("ppphuang");
        System.out.println(ppphuang);
    }
}
```

### 异步调用

1. 首先继承`AsyncReceiveHandler`实现抽象方法。

```java
public class TestCallBackHandler extends AsyncReceiveHandler {
    @Override
    public void callBack(Object context, Object result) {
        System.out.println(context);
        System.out.println(result);
    }
}
```

2. 手动获取异步代理对象。

```java
class RpcSpringStarterApplicationTests {

    @Autowired
    ClientProxyFactory clientProxyFactory;

    @Test
    void testAsync() throws InterruptedException {
        //获取异步代理类
        HelloService proxy = clientProxyFactory.getProxy(HelloService.class, true);
        //继承AsyncReceiveHandler 实现抽象方法 然后实例化自定义的回调对象
        TestCallBackHandler callBackHandler = new TestCallBackHandler();
        //设置的回调上下文以及回调对象
        ClientProxyFactory.setLocalAsyncContextAndAsyncReceiveHandler("context", callBackHandler);
        //异步回调方法同步返回空
        String ppphuang = proxy.hello("ppphuang");
        System.out.println(ppphuang);
        Thread.sleep(10000);
    }
}
```