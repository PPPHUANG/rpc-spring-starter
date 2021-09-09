package cn.ppphuang.rpcspringstarter.common.constants;

/**
 * 状态枚举
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
public enum RpcStatusEnum {
    /**
     * SUCCESS
     */
    SUCCESS(200, "SUCCESS"),

    /**
     * ERROR
     */
    ERROR(500, "ERROR"),

    /**
     * NOT_FOUND
     */
    NOT_FOUND(404, "NOT FOUND");

    private int code;

    private String desc;

    RpcStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
