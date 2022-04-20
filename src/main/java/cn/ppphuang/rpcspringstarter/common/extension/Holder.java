package cn.ppphuang.rpcspringstarter.common.extension;

/**
 * @Author: ppphuang
 * @Create: 2022/4/20
 */
public class Holder<T> {
    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
