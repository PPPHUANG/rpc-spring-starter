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
public @interface RpcService {
    /**
     * 服务发布名称 实现多接口时按注解设置的值发布服务
     */
    String value() default "";

    /**
     * 服务分组 服务接口有多个实现类时按注解设置的值分组
     */
    String group() default "";

    /**
     * 服务版本
     */
    String version() default "";
}
