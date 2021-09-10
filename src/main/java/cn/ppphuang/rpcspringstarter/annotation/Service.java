package cn.ppphuang.rpcspringstarter.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Service注解，提供RPC接口
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface Service {
    String value() default "";
}
