package cn.ppphuang.rpcspringstarter.common.protocol;

import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;

import java.io.*;

/**
 * Java序列化消息协议
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
public class JavaSerializeMessageProtocol implements MessageProtocol {

    private byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(o);
        return bout.toByteArray();
    }

    @Override
    public byte[] marshallingRequest(RpcRequest request) throws Exception {
        return serialize(request);
    }

    @Override
    public RpcRequest unmarshallingRequest(byte[] data) throws Exception {
        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(data));
        return (RpcRequest) inputStream.readObject();
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) throws Exception {
        return serialize(response);
    }

    @Override
    public RpcResponse unmarshallingResponse(byte[] data) throws Exception {
        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(data));
        return (RpcResponse) inputStream.readObject();
    }
}
