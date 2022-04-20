package cn.ppphuang.rpcspringstarter.annotation;

import java.lang.annotation.*;

/**
 * SPI扩展名称，为了兼容java spi的META-INF/services/下文件的格式
 * <p>
 * 当如下xxx=xxxx格式时，按照xxx名称
 * jdk=com.alibaba.dubbo.common.compiler.support.JdkCompiler
 * javassist=com.alibaba.dubbo.common.compiler.support.JavassistCompiler
 * <p>
 * 当如下xxxx格式时，按照SPIExtension.value名称
 * com.alibaba.dubbo.common.compiler.support.JdkCompiler
 *
 * @Author: ppphuang
 * @Create: 2022/4/20
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SPIExtension {
    String value();
}
