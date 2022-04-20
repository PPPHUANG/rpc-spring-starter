package cn.ppphuang.rpcspringstarter.common.protocol;

import cn.ppphuang.rpcspringstarter.annotation.SPIExtension;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.util.SerializingUtil;

/**
 * ProtoBuf序列化消息协议
 *
 * @Author: ppphuang
 * @Create: 2021/9/14
 */
@SPIExtension(RpcConstant.PROTOCOL_PROTOBUF)
public class ProtoBufSerializeMessageProtocol implements MessageProtocol {
    @Override
    public byte[] marshallingRequest(RpcRequest request) throws Exception {
        return SerializingUtil.serialize(request);
    }

    @Override
    public RpcRequest unmarshallingRequest(byte[] data) throws Exception {
        return SerializingUtil.deserialize(data, RpcRequest.class);
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) throws Exception {
        return SerializingUtil.serialize(response);
    }

    @Override
    public RpcResponse unmarshallingResponse(byte[] data) throws Exception {
        return SerializingUtil.deserialize(data, RpcResponse.class);
    }
}
