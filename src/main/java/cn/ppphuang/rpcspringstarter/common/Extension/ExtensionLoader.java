package cn.ppphuang.rpcspringstarter.common.Extension;

import cn.ppphuang.rpcspringstarter.common.compresser.Compresser;
import cn.ppphuang.rpcspringstarter.common.protocol.MessageProtocol;

import java.util.Map;

/**
 * 扩展加载类
 *
 * @Author: ppphuang
 * @Create: 2022/4/4
 */
public class ExtensionLoader {
    public static Map<String, MessageProtocol> supportMessageProtocols;

    public static Map<String, Compresser> supportCompressers;

    public static void setSupportMessageProtocols(Map<String, MessageProtocol> supportMessageProtocols) {
        ExtensionLoader.supportMessageProtocols = supportMessageProtocols;
    }

    public static void setSupportCompressers(Map<String, Compresser> supportCompressers) {
        ExtensionLoader.supportCompressers = supportCompressers;
    }
}
