package com.jacky.config;

import com.jacky.service.MailService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author jacky
 * @time 2020-12-22 14:11
 * @discription 测试AOP避坑指南模块
 *              1、访问被注入的Bean时，总是调用方法而非直接访问字段
 *              2、编写Bean时，如果可能会被代理，就不要编写public final方法
 *
 *              遇到CglibAopProxy的相关日志，务必要仔细检查，防止因为AOP出现NPE异常。
 */

@Component
@Configuration
@ComponentScan(value = "com.jacky")
@EnableAspectJAutoProxy
public class AopConfig {

    public static void main(String[] args){
        ApplicationContext context = new AnnotationConfigApplicationContext(AopConfig.class);
        /**
         * 启用AOP后，此刻，从ApplicationContext中获取的UserService实例是proxy，
         * 注入到MailService中的UserService实例也是proxy。
         */
        MailService mailService = context.getBean(MailService.class);
        System.out.println(mailService.sendMail());
    }
}
