package cn.ppphuang.rpcspringstarter.server;

import cn.ppphuang.rpcspringstarter.server.register.ServerRegister;
import lombok.extern.slf4j.Slf4j;

/**
 * 关机函数
 *
 * @Author: ppphuang
 * @Create: 2022/5/21
 */
@Slf4j
public class ShutdownHook {
    public static void registerShutdownHook(ServerRegister register, RpcServer rpcServer) {
        log.info("registerShutdownHook");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //先在注册中心移除，避免客户端请求到即将停止的服务
            register.remove();
            log.info("ShutdownHook register remove");
            try {
                // 休息3秒 尽量让已到达的请求可以处理完
                log.info("ShutdownHook time sleep 3 second, then stop rpcServer");
                Thread.sleep(3000);
                rpcServer.stop();
                log.info("ShutdownHook rpcServer stopped");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
