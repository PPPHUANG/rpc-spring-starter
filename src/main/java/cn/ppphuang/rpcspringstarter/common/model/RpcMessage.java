package cn.ppphuang.rpcspringstarter.common.model;


import cn.ppphuang.rpcspringstarter.common.constants.RpcCompressEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcProtocolEnum;

/**
 * @author ppphuang
 * @Create: 2022/4/4
 */
public class RpcMessage {
    RpcCompressEnum compress;
    RpcProtocolEnum protocol;
    byte type;
    Object data;

    public RpcCompressEnum getCompress() {
        return compress;
    }

    public void setCompress(RpcCompressEnum compress) {
        this.compress = compress;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public RpcProtocolEnum getProtocol() {
        return protocol;
    }

    public void setProtocol(RpcProtocolEnum protocol) {
        this.protocol = protocol;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RpcMessage{" +
                "compress=" + compress +
                ", protocol=" + protocol +
                ", type=" + type +
                ", data=" + data +
                '}';
    }
}
