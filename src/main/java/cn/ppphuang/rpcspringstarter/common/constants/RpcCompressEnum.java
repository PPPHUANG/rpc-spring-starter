package cn.ppphuang.rpcspringstarter.common.constants;

/**
 * 压缩协议枚举
 *
 * @Author: ppphuang
 * @Create: 2022/4/4
 */
public enum RpcCompressEnum {

    /**
     * unzip
     */
    UNZIP((byte) 0x00, "unzip"),

    /**
     * Gzip
     */
    GZIP((byte) 0x01, "Gzip");

    private byte code;

    private String compress;

    RpcCompressEnum(byte code, String compress) {
        this.code = code;
        this.compress = compress;
    }

    public byte getCode() {
        return code;
    }

    public String getCompress() {
        return compress;
    }

    public static RpcCompressEnum getCompress(byte code) {
        for (RpcCompressEnum c : RpcCompressEnum.values()) {
            if (c.code == code) {
                return c;
            }
        }
        return null;
    }

    public static RpcCompressEnum getCompress(String compress) {
        for (RpcCompressEnum c : RpcCompressEnum.values()) {
            if (c.compress.equals(compress)) {
                return c;
            }
        }
        return null;
    }
}
