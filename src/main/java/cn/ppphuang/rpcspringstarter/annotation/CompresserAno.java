package cn.ppphuang.rpcspringstarter.annotation;

import java.lang.annotation.*;

/**
 * 解压缩注解
 *
 * @Author: ppphuang
 * @Create: 2022/4/2
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CompresserAno {
    String value() default "";
}
