package cn.ppphuang.rpcspringstarter;

import cn.ppphuang.rpcspringstarter.client.async.AsyncExecutor;
import cn.ppphuang.rpcspringstarter.client.async.DefaultValueHandle;
import cn.ppphuang.rpcspringstarter.client.async.LogErrorAction;
import cn.ppphuang.rpcspringstarter.client.async.TestCallBackHandler;
import cn.ppphuang.rpcspringstarter.client.net.ClientProxyFactory;
import cn.ppphuang.rpcspringstarter.service.HelloService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;

@SpringBootTest
class RpcSpringStarterApplicationTests {

    @Autowired
    ClientProxyFactory clientProxyFactory;

    @Test
    void contextLoads() {
    }

    @Test
    void testSync() throws InterruptedException {
        HelloService proxy = clientProxyFactory.getProxy(HelloService.class);
        String ppphuang = proxy.hello("ppphuang");
        System.out.println(ppphuang);
    }

    @Test
    void testSyncGroup() throws InterruptedException {
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

    @Test
    void testAsync() throws InterruptedException {
        HelloService proxy = clientProxyFactory.getProxy(HelloService.class, "group2", "version2", true);
        TestCallBackHandler callBackHandler = new TestCallBackHandler();
        ClientProxyFactory.setLocalAsyncContextAndAsyncReceiveHandler("context", callBackHandler);
        String ppphuang = proxy.hello("ppphuang");
        System.out.println(ppphuang);
        Thread.sleep(10000);
    }

    @Test
    void testCompletableFutureAsync() {
        //?????????????????????
        AsyncExecutor<HelloService> helloServiceAsyncExecutor = new AsyncExecutor<>(clientProxyFactory, HelloService.class, "", "");
        String name = "ppphuang";
        //????????????????????????????????????????????????CompletableFuture
        CompletableFuture<String> ppphuang = helloServiceAsyncExecutor.async(server -> server.hello(name));
        //??????whenComplete?????????????????? ????????????handle?????????????????????
        CompletableFuture<String> stringCompletableFuture = ppphuang.whenComplete(new LogErrorAction<>("server.hello", name));
        //??????handle?????? ??????????????????????????? ?????????????????????????????? ????????????
        CompletableFuture<String> hp = stringCompletableFuture.handle(new DefaultValueHandle<>(true, "hp", "server.hello", name));
        //????????????
        System.out.println(hp.join());
    }
}
