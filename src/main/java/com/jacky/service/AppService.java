package com.jacky.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.stream.Collector;
import java.util.stream.Collectors;


/**
 * @author jacky
 * @time 2020-12-06 19:25
 * @discription
 */

@Component
public class AppService {

    /**
     * 可以直接指定常量
     */
    @Value("1")
    private int version;

    /**
     * Spring提供了Resource类便于注入资源文件。
     *
     * 注入Resource最常用的方式是通过classpath，
     * 即类似classpath:/logo.txt表示在classpath中搜索logo.txt文件，
     * 然后直接调用Resource.getInputStream()就可以获得到输入流，避免了自己搜索文件的代码
     */
    @Value("classpath:/logo.txt")
    private Resource resource;

    private String logo;

    //初始化方法
    @PostConstruct
    public void init() throws IOException{
        System.out.println("容器初始化方法开始...");
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))){
            this.logo = reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * 容器销毁方法：
     * Spring只根据Annotation查找无参数方法，对方法名不做要求
     */
    @PreDestroy
    public void shutdown(){
        System.out.println("容器销毁方法开始...");
    }


    public void printLogo() {
        System.out.println(logo);
        System.out.println("app.version: " + version);
    }


}
