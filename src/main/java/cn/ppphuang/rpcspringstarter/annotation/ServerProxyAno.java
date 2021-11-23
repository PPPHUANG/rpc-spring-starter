package cn.ppphuang.rpcspringstarter.annotation;

import java.lang.annotation.*;

/**
 * 服务代理类型
 *
 * @Author: ppphuang
 * @Create: 2021/11/23
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServerProxyAno {
    String value() default "";
}
