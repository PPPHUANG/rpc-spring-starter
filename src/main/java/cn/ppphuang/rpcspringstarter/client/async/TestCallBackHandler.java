package cn.ppphuang.rpcspringstarter.client.async;

/**
 * 客户端异步回调 供test使用
 *
 * @Author: ppphuang
 * @Create: 2021/12/03
 */
public class TestCallBackHandler extends AsyncReceiveHandler {
    @Override
    public void callBack(Object context, Object result) {
        System.out.println(context);
        System.out.println(result);
    }
}
