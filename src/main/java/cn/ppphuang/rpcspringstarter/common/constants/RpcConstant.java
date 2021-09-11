package cn.ppphuang.rpcspringstarter.common.constants;

/**
 * @Author: ppphuang
 * @Create: 2021/9/8
 */
public class RpcConstant {
    private RpcConstant() {
    }

    /**
     * Zookeeper服务注册地址
     */
    public static final String ZK_SERVICE_PATH = "/rpc";

    /**
     * zk路径分隔符
     */
    public static final String ZK_PATH_DELIMITER = "/";


    /**
     * 编码格式
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * java序列化协议
     */
    public static final String PROTOCOL_JAVA = "java";

}
