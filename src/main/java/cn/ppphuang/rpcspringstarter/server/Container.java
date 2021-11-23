package cn.ppphuang.rpcspringstarter.server;

/**
 * 服务对象容器
 *
 * @Author: ppphuang
 * @Create: 2021/11/23
 */
public class Container {
    /**
     * spring启动之后存储springContext
     */
    private static Object SpringContext = null;

    public static Object getSpringContext() {
        return SpringContext;
    }

    public static void setSpringContext(Object springContext) {
        SpringContext = springContext;
    }
}
