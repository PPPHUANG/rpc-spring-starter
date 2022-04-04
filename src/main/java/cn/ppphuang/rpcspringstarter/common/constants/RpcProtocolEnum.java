package cn.ppphuang.rpcspringstarter.common.constants;

/**
 * 序列化协议枚举
 *
 * @Author: ppphuang
 * @Create: 2022/4/4
 */
public enum RpcProtocolEnum {

    /**
     * kryo
     */
    KRYO((byte) 0x01, RpcConstant.PROTOCOL_KRYO),

    /**
     * JAVA
     */
    JAVA((byte) 0x02, RpcConstant.PROTOCOL_JAVA),

    /**
     * PROTOBUF
     */
    PROTOBUF((byte) 0x03, RpcConstant.PROTOCOL_PROTOBUF);

    private byte code;

    private String protocol;

    RpcProtocolEnum(byte code, String protocol) {
        this.code = code;
        this.protocol = protocol;
    }

    public byte getCode() {
        return code;
    }

    public String getProtocol() {
        return protocol;
    }

    public static RpcProtocolEnum getProtocol(byte code) {
        for (RpcProtocolEnum c : RpcProtocolEnum.values()) {
            if (c.code == code) {
                return c;
            }
        }
        return null;
    }

    public static RpcProtocolEnum getProtocol(String protocol) {
        for (RpcProtocolEnum c : RpcProtocolEnum.values()) {
            if (c.protocol.equals(protocol)) {
                return c;
            }
        }
        return null;
    }
}
