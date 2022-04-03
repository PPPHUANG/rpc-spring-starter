package cn.ppphuang.rpcspringstarter.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 注入远程服务
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InjectService {
    /**
     * 服务分组
     */
    String group() default "";

    /**
     * 服务版本
     */
    String version() default "";
}
