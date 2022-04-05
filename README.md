# rpc-spring-starter

> 基于Netty的RPC框架

## TODO

- [X] Netty通讯
- [x] 基于ZK的服务注册发现
- [x] 客户端负载均衡
- [x] 支持Java、ProtoBuf、Kryo序列化
- [X] 增加Netty编解码器
- [x] 支持可配置的服务端代理模式，可选反射调用、字节码增强
- [x] 支持异步调用
- [x] 支持Gzip压缩
- [x] 支持服务分组与服务版本
- [x] 连接保持心跳
- [x] 增加传输协议
- [ ] 调用鉴权
- [ ] 调用监控、告警
- [ ] 调用限流、熔断、降级
- [ ] 支持灰度

1. 克隆本项目到本地install。

```bash
mvn  clean install -DskipTests=true
```

2. 添加maven依赖到你的`SpringBoot`项目中。

 ```xml

<dependency>
    <groupId>cn.ppphuang</groupId>
    <artifactId>rpc-spring-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
 ```

3. 启动ZK。

4. 默认配置项在`RpcConfig`类中，可以通过`application.properties`来覆盖需要修改的配置项。

```properties
#是否启用rpc 默认启用
hp.rpc.enable=true
#注册中心地址
hp.rpc.register-address=127.0.0.1:2128
#服务暴露端口
hp.rpc.server-port=9999
#客户端随机负载均衡 默认：random，可选 random 随机 、round 轮询、 加权轮询 weightRound 、平滑加权轮询 smoothWeightRound
hp.rpc.load-balance=random
#序列化协议 默认：kryo, 可选 kryo 、 java 、 protobuf
hp.rpc.protocol=kryo
#服务是否启用压缩算法 默认：false
hp.rpc.enable-compress=false
#压缩算法 默认：Gzip, 目前可选 Gzip
hp.rpc.compress=Gzip
#服务代理类型 默认：javassist， 可选 reflect 反射调用、 javassist 字节码生成代理类调用
hp.rpc.server-proxy-type=javassist
#服务权重
hp.rpc.weight=1
```

## 传输协议

<pre>
 *   0     1     2     3     4         5     6     7     8     9      10         11         12    13    14    15   16
 *   +-----+-----+-----+-----+---------+-----+-----+-----+-----+------+----------+----------+-----+-----+-----+-----+
 *   |   magic   code        | version |      full length      | type | protocol | compress |       RequestId       |
 *   +-----------------------+---------+-----------------------+------+----------+----------+-----------------------+
 *   |                                                                                                              |
 *   |                                             body                                                             |
 *   |                                                                                                              |
 *   |                                            ... ...                                                           |
 *   +--------------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔数）      1B version（协议版本）   4B full length（消息长度）    1B type（消息类型）
 * 1B  compress（压缩类型）     1B protocol（序列化类型）   4B requestId（请求的Id）
 * body（object类型数据）
 </pre>

## 服务端

1. 定义服务接口。

```java
public interface HelloService {
    String hello(String name);
}
```

2. 实现服务接口并通过`@RpcService`注解发布服务。

 ```java
import cn.ppphuang.rpcspringstarter.annotation.RpcService;
import cn.ppphuang.rpcspringstarter.annotation.Service;
import cn.ppphuang.rpcspringstarter.service.HelloService;

@RpcService
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return name + "hello";
    }
}
 ```

 ```java
import cn.ppphuang.rpcspringstarter.annotation.RpcService;
import cn.ppphuang.rpcspringstarter.service.HelloService;

@RpcService(group = "group1")
public class HelloServiceGroup1Impl implements HelloService {

    @Override
    public String hello(String name) {
        return name + "hello group1";
    }
}
 ```

 ```java
import cn.ppphuang.rpcspringstarter.annotation.RpcService;
import cn.ppphuang.rpcspringstarter.service.HelloService;

@RpcService(group = "group1", version = "version1")
public class HelloServiceGroup1Version1Impl implements HelloService {

    @Override
    public String hello(String name) {
        return name + "hello group1 version1";
    }
}
 ```

当服务实现类实现了多个接口时，需要通过`value`注明该方法提供的是哪个服务接口的实现

 ```java
import cn.ppphuang.rpcspringstarter.annotation.RpcService;
import cn.ppphuang.rpcspringstarter.service.HelloService;

@RpcService(value = "cn.ppphuang.rpcspringstarter.service.HelloService", group = "group2", version = "version2")
public class HelloServiceGroup2Version2Impl implements HelloService, PersionService {

    @Override
    public String hello(String name) {
        return name + "hello group2 version2";
    }
}
 ```

## 客户端

### 同步调用

1. 使用`@InjectService`注解注入远程服务。

 ```java
import cn.ppphuang.rpcspringstarter.annotation.InjectService;
import cn.ppphuang.rpcspringstarter.service.HelloService;
import org.springframework.stereotype.Service;

@Service
public class TestService1 {
    @InjectService
    HelloService helloService;

    @InjectService(group = "group1")
    HelloService helloServiceGroup1;

    @InjectService(group = "group1", version = "version1")
    HelloService helloServiceGroup1Version1;

    @InjectService(group = "group2", version = "version2")
    HelloService helloServiceGroup2Version2;

    public String sayHai(String name) {
        return helloService.hello(name);
    }

    public String sayHaiGroup1(String name) {
        return helloService.hello(name);
    }

    public String sayHaiGroup1Version1(String name) {
        return helloService.hello(name);
    }

    public String sayHaiGroup2Version2(String name) {
        return helloService.hello(name);
    }
}
 ```

2. 手动获取代理对象。

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

    @Test
    void testSyncGroupVersion() throws InterruptedException {
        HelloService proxy = clientProxyFactory.getProxy(HelloService.class, "group1");
        String ppphuang = proxy.hello("ppphuang");
        System.out.println(ppphuang);
    }

    @Test
    void testSyncGroupVersion() throws InterruptedException {
        HelloService proxy = clientProxyFactory.getProxy(HelloService.class, "group1", "version1");
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

    @Override
    public void onException(Object context, Object result, Exception e) {
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
        HelloService proxy = clientProxyFactory.getProxy(HelloService.class, "group1", "version1", true);
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