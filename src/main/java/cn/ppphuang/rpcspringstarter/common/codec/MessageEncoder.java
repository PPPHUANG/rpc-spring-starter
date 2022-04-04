package cn.ppphuang.rpcspringstarter.common.codec;

import cn.ppphuang.rpcspringstarter.common.Extension.ExtensionLoader;
import cn.ppphuang.rpcspringstarter.common.compresser.Compresser;
import cn.ppphuang.rpcspringstarter.common.constants.RpcCompressEnum;
import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import cn.ppphuang.rpcspringstarter.common.model.RpcMessage;
import cn.ppphuang.rpcspringstarter.common.model.RpcRequest;
import cn.ppphuang.rpcspringstarter.common.model.RpcResponse;
import cn.ppphuang.rpcspringstarter.common.protocol.MessageProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 传输编码协议
 *
 * <pre>
 *   0     1     2     3     4         5     6     7     8     9      10         11         12    13    14    15   16
 *   +-----+-----+-----+-----+---------+-----+-----+-----+-----+------+----------+----------+-----+-----+-----+-----+
 *   |   magic   code        | version |      full length      | type | protocol | compress |       RequestId       |
 *   +-----------------------+---------+-----------------------+------+----------+----------+-----------------------+
 *   |                                                                                                              |
 *   |                                             body                                                             |
 *   |                                                                                                              |
 *   |                                            ... ...                                                           |
 *   +--------------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔数）      1B version（协议版本）   4B full length（消息长度）    1B type（消息类型）
 * 1B  compress（压缩类型）     1B codec（序列化类型）   4B requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 *
 * @Author: ppphuang
 * @Create: 2022/4/4
 */
@Slf4j
public class MessageEncoder extends MessageToByteEncoder<RpcMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {
        try {
            byteBuf.writeBytes(RpcConstant.MAGIC_NUMBER);
            byteBuf.writeByte(RpcConstant.VERSION);
            // leave a place to write the value of full length
            byteBuf.writerIndex(byteBuf.writerIndex() + 4);
            byte messageType = rpcMessage.getType();
            byteBuf.writeByte(messageType);
            byteBuf.writeByte(rpcMessage.getProtocol().getCode());
            byteBuf.writeByte(rpcMessage.getCompress().getCode());
            byteBuf.writeInt(0);
            // build full length
            byte[] bodyBytes = null;
            int fullLength = RpcConstant.HEAD_LENGTH;
            // if messageType is not heartbeat message,fullLength = head length + body length
            if (messageType != RpcConstant.HEARTBEAT_REQUEST_TYPE && messageType != RpcConstant.HEARTBEAT_RESPONSE_TYPE) {
                // serialize the object
                MessageProtocol messageProtocol = ExtensionLoader.supportMessageProtocols.get(rpcMessage.getProtocol().getProtocol());

                if (messageType == RpcConstant.REQUEST_TYPE) {
                    bodyBytes = messageProtocol.marshallingRequest((RpcRequest) rpcMessage.getData());
                } else {
                    bodyBytes = messageProtocol.marshallingResponse((RpcResponse) rpcMessage.getData());
                }

                // compress the bytes
                if (rpcMessage.getCompress() != RpcCompressEnum.UNZIP) {
                    Compresser compresser = ExtensionLoader.supportCompressers.get(rpcMessage.getCompress().getCompress());
                    log.debug("before compress length:{}", bodyBytes.length);
                    bodyBytes = compresser.compress(bodyBytes);
                    log.debug("after compress length:{}", bodyBytes.length);
                }
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                byteBuf.writeBytes(bodyBytes);
            }
            int writeIndex = byteBuf.writerIndex();
            byteBuf.writerIndex(writeIndex - fullLength + RpcConstant.MAGIC_NUMBER.length + 1);
            byteBuf.writeInt(fullLength);
            byteBuf.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("encode request error!", e);
        }

    }
}
