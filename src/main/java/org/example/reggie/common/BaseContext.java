package org.example.reggie.common;

/**
 * ThreadLocal封装工具类，用户保存和获取当前登录用户的id
 * 作用范围是单个线程内
 */
public class BaseContext {
    private static ThreadLocal<Long>threadLocal =new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }

}
