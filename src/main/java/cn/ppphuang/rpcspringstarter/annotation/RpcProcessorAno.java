package cn.ppphuang.rpcspringstarter.annotation;

import java.lang.annotation.*;

/**
 * 服务代理类型Processor
 *
 * @Author: ppphuang
 * @Create: 2021/11/24
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcProcessorAno {
    String value() default "";
}
