package cn.ppphuang.rpcspringstarter.exception;

/**
 * 自定义异常类
 *
 * @Author: ppphuang
 * @Create: 2021/9/9
 */
public class RpcException extends RuntimeException{
    public RpcException(String message) {
        super(message);
    }
}
