package cn.ppphuang.rpcspringstarter.annotation;

import java.lang.annotation.*;

/**
 * 负载均衡注解
 *
 * @Author: ppphuang
 * @Create: 2021/9/12
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoadBalanceAno {
    String value() default "";
}
