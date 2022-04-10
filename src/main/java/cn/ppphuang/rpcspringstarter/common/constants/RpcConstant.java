package cn.ppphuang.rpcspringstarter.common.constants;

import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.server.Container;
import cn.ppphuang.rpcspringstarter.server.InvokeProxy;
import cn.ppphuang.rpcspringstarter.util.ConvertUtil;

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
     * protobuf序列化协议
     */
    public static final String PROTOCOL_PROTOBUF = "protobuf";

    /**
     * kryo序列化协议
     */
    public static final String PROTOCOL_KRYO = "kryo";

    /**
     * 解压缩算法
     */
    public static final String COMPRESS_GZIP = "Gzip";

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

    /**
     * 服务代理类型 reflect： 反射调用
     */
    public static final String SERVER_PROXY_TYPE_REFLECT = "reflect";

    /**
     * javassist： 字节码生成代理类调用
     */
    public static final String SERVER_PROXY_TYPE_JAVASSIST = "javassist";

    /**
     * 服务容器类名称
     */
    public static final String CONTAINER_CLASS_NAME = Container.class.getName();

    /**
     * 代理类实现接口名称
     */
    public static final String INVOKE_PROXY_INTERFACE_NAME = InvokeProxy.class.getName();

    /**
     * RPC_REQUEST_CLASS_NAME
     */
    public static final String RPC_REQUEST_CLASS_NAME = RpcRequest.class.getName();

    /**
     * RPC_REQUEST_CLASS_NAME
     */
    public static final String RPC_RESPONSE_CLASS_NAME = RpcResponse.class.getName();

    /**
     * CONVERT_UTIL_CLASS_NAME
     */
    public static final String CONVERT_UTIL_CLASS_NAME = ConvertUtil.class.getName();

    /**
     * EXCEPTION_CLASS_NAME
     */
    public static final String EXCEPTION_CLASS_NAME = Exception.class.getName();

    /**
     * OBJECT_CLASS_NAME
     */
    public static final String OBJECT_CLASS_NAME = Object.class.getName();

    /**
     * MAGIC_NUMBER
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};

    /**
     * VERSION
     */
    public static final byte VERSION = 1;

    /**
     * TOTAL_LENGTH
     */
    public static final byte TOTAL_LENGTH = 16;

    /**
     * REQUEST_TYPE
     */
    public static final byte REQUEST_TYPE = 1;

    /**
     * RESPONSE_TYPE
     */
    public static final byte RESPONSE_TYPE = 2;

    /**
     * HEARTBEAT_REQUEST_TYPE
     */
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;

    /**
     * HEARTBEAT_RESPONSE_TYPE
     */
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;

    /**
     * PING
     */
    public static final String PING = "ping";

    /**
     * PONG
     */
    public static final String PONG = "pong";

    /**
     * MAX_FRAME_LENGTH
     */
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

    /**
     * HEAD_LENGTH
     */
    public static final int HEAD_LENGTH = 16;

    /**
     * 等待响应最大时间
     */
    public static final int RESPONSE_WAIT_TIME = 8;

    /**
     * 等待通道建立超时时间
     */
    public static final int CHANNEL_WAIT_TIME = 4;

    /**
     * Netty连接池获取连接超时时间
     */
    public static final int POOL_CHANNEL_WAIT_TIME = 1;

    /**
     * Integer.MAX_VALUE调整为100000，避免oom风险
     */
    public static final int MAX_PENDING_ACQUIRES = 100000;
}
