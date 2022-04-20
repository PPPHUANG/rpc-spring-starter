package cn.ppphuang.rpcspringstarter.common.extension;

import cn.ppphuang.rpcspringstarter.annotation.SPIExtension;
import cn.ppphuang.rpcspringstarter.annotation.SPI;
import cn.ppphuang.rpcspringstarter.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 扩展加载类
 * <p>
 * refer to dubbo spi: <a href="https://dubbo.apache.org/zh/docs/concepts/extensibility/#dubbo-%E6%89%A9%E5%B1%95%E5%8A%A0%E8%BD%BD%E6%B5%81%E7%A8%8B">https://dubbo.apache.org/zh/docs/concepts/extensibility/#dubbo-%E6%89%A9%E5%B1%95%E5%8A%A0%E8%BD%BD%E6%B5%81%E7%A8%8B</a>
 *
 * @Author: ppphuang
 * @Create: 2022/4/20
 * @see <a href="https://github.com/apache/dubbo/blob/2.5.x/dubbo-common/src/main/java/com/alibaba/dubbo/common/extension/ExtensionLoader.java">...</a>
 */
@Slf4j
public class ExtensionLoader<T> {

    private static final String SERVICES_DIRECTORY = "META-INF/services/";
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    private final Class<?> type;
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type(\" + type + \") is not interface!");
        }
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type(" + type +
                    ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
        }
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    /**
     * Find the extension with the given name. If the specified name is not found, then {@link IllegalStateException}
     * will be thrown.
     */
    @SuppressWarnings("unchecked")
    public T getExtension(String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Extension name == null");
        }
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }


    @SuppressWarnings("unchecked")
    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new IllegalStateException("No such extension " + type.getName() + " by name " + name);
        }
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Throwable t) {
                throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                        type + ")  could not be instantiated: " + t.getMessage(), t);
            }
        }
        return instance;
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    private Map<String, Class<?>> loadExtensionClasses() {
        Map<String, Class<?>> extensionClasses = new HashMap<>();
        loadFile(extensionClasses, ExtensionLoader.SERVICES_DIRECTORY);
        return extensionClasses;
    }

    private void loadFile(Map<String, Class<?>> extensionClasses, String dir) {
        String fileName = dir + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (Throwable t) {
            log.error("Exception when load extension class(interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        final int ei = line.indexOf('=');

                        /*
                        为了兼容java SPI的META-INF/services/下文件的格式
                        当如下xxx=xxxx格式时，按照xxx名称
                        jdk=com.alibaba.dubbo.common.compiler.support.JdkCompiler

                        当如下xxxx格式时，按照SPIExtension.value名称
                        com.alibaba.dubbo.common.compiler.support.JdkCompiler
                         */
                        if (ei > 0) {
                            String name = line.substring(0, ei).trim();
                            String clazzName = line.substring(ei + 1).trim();
                            if (name.length() > 0 && clazzName.length() > 0) {
                                Class<?> clazz = classLoader.loadClass(clazzName);
                                extensionClasses.put(name, clazz);
                            }
                        } else {
                            Class<?> clazz = classLoader.loadClass(line);
                            SPIExtension annotation = clazz.getAnnotation(SPIExtension.class);
                            if (annotation == null || StringUtil.isBlank(annotation.value())) {
                                throw new IllegalArgumentException("Extension type(" + type +
                                        ") clazz " + line + "is illegal, because WITHOUT @" + SPIExtension.class.getSimpleName() + " Annotation!");
                            }
                            String name = annotation.value();
                            extensionClasses.put(name, clazz);
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
