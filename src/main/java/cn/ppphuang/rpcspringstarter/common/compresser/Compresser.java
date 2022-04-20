package cn.ppphuang.rpcspringstarter.common.compresser;


import cn.ppphuang.rpcspringstarter.annotation.SPI;

/**
 * 解压缩接口
 *
 * @Author: ppphuang
 * @Create: 2022/4/2
 */
@SPI
public interface Compresser {
    /**
     * 压缩
     *
     * @param bytes
     * @return
     */
    byte[] compress(byte[] bytes);


    /**
     * 解压
     *
     * @param bytes
     * @return
     */
    byte[] decompress(byte[] bytes);
}
