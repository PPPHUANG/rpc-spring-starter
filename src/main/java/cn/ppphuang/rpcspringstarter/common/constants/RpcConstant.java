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


    /**
     * 客户端随机负载均衡
     */
    public static final String BALANCE_RANDOM = "random";

    /**
     * 客户端轮询负载均衡
     */
    public static final String BALANCE_FULL_ROUND = "round";

    /**
     * 客户端加权轮询负载均衡
     */
    public static final String BALANCE_WEIGHT_ROUND = "weightRound";

    /**
     * 客户端平滑加权轮询负载均衡
     */
    public static final String BALANCE_SMOOTH_WEIGHT_ROUND = "smoothWeightRound";
}
