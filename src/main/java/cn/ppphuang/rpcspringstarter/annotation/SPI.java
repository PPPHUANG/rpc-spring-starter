package cn.ppphuang.rpcspringstarter.annotation;

import java.lang.annotation.*;

/**
 * SPI注解
 *
 * @Author: ppphuang
 * @Create: 2022/4/20
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SPI {
}
