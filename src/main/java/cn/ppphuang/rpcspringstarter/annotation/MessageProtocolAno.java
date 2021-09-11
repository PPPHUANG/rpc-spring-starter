package cn.ppphuang.rpcspringstarter.annotation;

import java.lang.annotation.*;

/**
 * 消息协议注解
 *
 * @Author: ppphuang
 * @Create: 2021/9/11
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageProtocolAno {
    String value() default "";
}
