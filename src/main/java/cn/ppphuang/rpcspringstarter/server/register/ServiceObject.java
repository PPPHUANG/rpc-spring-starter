package cn.ppphuang.rpcspringstarter.server.register;

/**
 * 服务持有对象，保存具体的服务信息
 *
 * @Author: ppphuang
 * @Create: 2021/9/10
 */
public class ServiceObject {
    /**
     * 服务名称
     */
    private String name;

    /**
     * 服务Class
     */
    private Class<?> clazz;

    /**
     * 具体服务
     */
    private Object obj;

    /**
     * 服务分组
     */
    private String group;

    /**
     * 服务版本
     */
    private String version;

    public ServiceObject(String name, Class<?> clazz, Object obj, String group, String version) {
        this.name = name;
        this.clazz = clazz;
        this.obj = obj;
        this.group = group;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
