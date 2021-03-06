package cn.ppphuang.rpcspringstarter.service;

import java.util.List;

/**
 * 测试服务接口
 *
 * @Author: ppphuang
 * @Create: 2021/9/11
 */
public interface HelloService {
    /**
     * @param name String
     * @param age  age
     * @return age
     */
    String hello(String name, Integer age);

    /**
     * @param name String
     * @return age
     */
    String hello(String name);

    /**
     * @param age
     * @return
     */
    Integer helloInteger(Integer age);

    /**
     * @param age int
     * @return int
     */
    int helloInt(int age);

    /**
     * @param age age
     * @return byte
     */
    byte helloByte(byte age);

    /**
     * @param age Boolean
     * @return boolean
     */
    boolean helloBoolean(Boolean age);

    /**
     * @param name String
     * @param age  Integer
     * @return Result<Person>
     */
    Result<List<Person>> helloPerson(String name, Integer age);

    /**
     * @param age Integer
     * @return List<Integer>
     */
    List<Integer> helloList(Integer age);
}
