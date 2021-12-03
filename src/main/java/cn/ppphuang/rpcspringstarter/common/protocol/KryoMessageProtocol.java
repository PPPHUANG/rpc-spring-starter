package cn.ppphuang.rpcspringstarter.common.protocol;

import cn.ppphuang.rpcspringstarter.annotation.MessageProtocolAno;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


/**
 * Kryo序列化消息协议
 * kryo线程不安全
 *
 * @Author: ppphuang
 * @Create: 2021/9/14
 */
@MessageProtocolAno(RpcConstant.PROTOCOL_KRYO)
public class KryoMessageProtocol implements MessageProtocol {
    private static final ThreadLocal<Kryo> kryoLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            kryo.setReferences(false);
            kryo.register(RpcRequest.class);
            kryo.register(RpcResponse.class);
            DefaultInstantiatorStrategy strategy = (DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy();
            strategy.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }
    };

    /**
     * 获得当前线程的 Kryo 实例
     *
     * @return 当前线程的 Kryo 实例
     */
    public static Kryo getInstance() {
        return kryoLocal.get();
    }

    @Override
    public byte[] marshallingRequest(RpcRequest request) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Output output = new Output(bout);
        Kryo kryo = getInstance();
        kryo.writeClassAndObject(output, request);
        byte[] bytes = output.toBytes();
        output.flush();
        return bytes;
    }

    @Override
    public RpcRequest unmarshallingRequest(byte[] data) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Input input = new Input(bin);
        Kryo kryo = getInstance();
        RpcRequest request = (RpcRequest) kryo.readClassAndObject(input);
        input.close();
        return request;
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Output output = new Output(bout);
        Kryo kryo = getInstance();
        kryo.writeClassAndObject(output, response);
        byte[] bytes = output.toBytes();
        output.flush();
        return bytes;
    }

    @Override
    public RpcResponse unmarshallingResponse(byte[] data) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Input input = new Input(bin);
        Kryo kryo = getInstance();
        RpcResponse response = (RpcResponse) kryo.readClassAndObject(input);
        input.close();
        return response;
    }
}
