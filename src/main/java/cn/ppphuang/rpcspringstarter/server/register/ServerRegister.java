package cn.ppphuang.rpcspringstarter.server.register;

/**
 * 服务注册器，定义服务注册规范
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
public interface ServerRegister {
    /**
     * 注册服务对象
     *
     * @param so
     * @throws Exception
     */
    void register(ServiceObject so) throws Exception;

    /**
     * 获取服务对象
     *
     * @param name
     * @return
     * @throws Exception
     */
    ServiceObject getServiceObject(String name) throws Exception;
}
