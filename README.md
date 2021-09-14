# rpc-spring-starter
## TODO
- [X] Netty通讯
- [x] 服务注册发现
- [x] 客户端负载均衡
- [x] Java、ProtoBuf、Kryo序列化
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
使用`@InjectService`注解注入远程方法
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

## 配置
默认配置项在`RpcConfig`类中，可以通过`application.properties`来覆盖需要修改的配置项。
