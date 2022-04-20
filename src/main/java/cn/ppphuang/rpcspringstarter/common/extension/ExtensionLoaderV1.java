package cn.ppphuang.rpcspringstarter.common.extension;

import cn.ppphuang.rpcspringstarter.common.compresser.Compresser;
import cn.ppphuang.rpcspringstarter.common.protocol.MessageProtocol;

import java.util.Map;

/**
 * 扩展加载类
 *
 * @Author: ppphuang
 * @Create: 2022/4/4
 */
public class ExtensionLoaderV1 {
    public static Map<String, MessageProtocol> supportMessageProtocols;

    public static Map<String, Compresser> supportCompressers;

    public static void setSupportMessageProtocols(Map<String, MessageProtocol> supportMessageProtocols) {
        ExtensionLoaderV1.supportMessageProtocols = supportMessageProtocols;
    }

    public static void setSupportCompressers(Map<String, Compresser> supportCompressers) {
        ExtensionLoaderV1.supportCompressers = supportCompressers;
    }
}
