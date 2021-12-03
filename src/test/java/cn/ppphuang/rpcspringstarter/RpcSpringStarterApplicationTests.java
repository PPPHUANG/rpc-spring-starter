package cn.ppphuang.rpcspringstarter;

import cn.ppphuang.rpcspringstarter.client.async.TestCallBackHandler;
import cn.ppphuang.rpcspringstarter.client.net.ClientProxyFactory;
import cn.ppphuang.rpcspringstarter.service.HelloService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RpcSpringStarterApplicationTests {

//    @Autowired
//    ClientProxyFactory clientProxyFactory;

    @Test
    void contextLoads() {
    }

    @Test
    void testSync() throws InterruptedException {
//        HelloService proxy = clientProxyFactory.getProxy(HelloService.class);
//        String ppphuang = proxy.hello("ppphuang");
//        System.out.println(ppphuang);
    }

    @Test
    void testAsync() throws InterruptedException {
//        HelloService proxy = clientProxyFactory.getProxy(HelloService.class, true);
//        TestCallBackHandler callBackHandler = new TestCallBackHandler();
//        ClientProxyFactory.setLocalAsyncContextAndAsyncReceiveHandler("context", callBackHandler);
//        String ppphuang = proxy.hello("ppphuang");
//        System.out.println(ppphuang);
//        Thread.sleep(10000);
    }
}
