package cn.ppphuang.rpcspringstarter.util;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

/**
 * ProtoBuf序列化消息协议工具类
 *
 * @Author: ppphuang
 * @Create: 2021/9/14
 */
public class SerializingUtil {

    /**
     * 将类序列化为byte数组
     *
     * @param source
     * @param <T>
     * @return
     */
    public static <T> byte[] serialize(T source) {
        Schema<T> schema = RuntimeSchema.getSchema((Class<T>) source.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        final byte[] result;
        try {
            result = ProtobufIOUtil.toByteArray(source, schema, buffer);
        } finally {
            buffer.clear();
        }
        return result;
    }

    /**
     * 将byte[]序列化为类
     *
     * @param source
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] source, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T t = schema.newMessage();
        ProtobufIOUtil.mergeFrom(source, t, schema);
        return t;
    }
}
