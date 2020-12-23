package com.jacky.service;

import com.jacky.annotation.MetricTime;
import com.jacky.condition.OnSmtpEnvCondition;
import com.jacky.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author jacky
 * @time 2020-12-04 14:43
 * @discription  用于在用户登录和注册成功后发送邮件通知
 */
@Component
//@Conditional(OnSmtpEnvCondition.class) //条件装配:根据@Conditional决定是否创建某个Bean
public class MailService {


    /**
     * 从javaBean中读取配置参数
     * #{}表示从JavaBean读取属性。
     *
     * 使用一个独立的JavaBean持有所有属性，然后在其他Bean中以#{bean.property}注入的好处是，多个Bean都可以引用同一个Bean的属性。
     */
    @Value("#{smtpConfig.host}")
    private String smtpHost;

    @Value("#{smtpConfig.port}")
    private int smtpPort;

    private ZoneId zoneId = ZoneId.systemDefault();

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public String getTime() {
        return ZonedDateTime.now(this.zoneId).format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    @MetricTime("loginMail")
    public void sendLoginMail(User user) {
        System.err.println(String.format("Hi, %s! You are logged in at %s", user.getName(), getTime()));
    }

    public void sendRegistrationMail(User user) {
        System.err.println(String.format("Welcome, %s!", user.getName()));
    }

    public void sendWelcomeMail() {
        System.out.println("at zone: " + zoneId);
        System.out.println("sent by smtp host: " + smtpHost);
        System.out.println("sent by smtp port: " + smtpPort);
    }


    /**
     * AOP避坑指南
     */
    @Autowired
    UserService userService;

    public String sendMail(){
        /**
         * 如果没有启用AOP，注入的是原始的UserService实例，那么一切正常，因为UserService实例的zoneId字段已经被正确初始化了。
         *
         * 如果启动了AOP，注入的是代理后的UserService$$EnhancerBySpringCGLIB实例，
         * 那么问题大了：获取的UserService$$EnhancerBySpringCGLIB实例的zoneId字段，永远为null。
         *
         * 那么问题来了：启用了AOP，如何修复？
         *
         * 修复很简单，只需要把直接访问字段的代码，改为通过方法访问。
         * 无论注入的UserService是原始实例还是代理实例，getZoneId()都能正常工作，因为代理类会覆写getZoneId()方法，并将其委托给原始实例：
         */
        ZoneId zoneId_old = userService.zoneId;
        ZoneId zoneId = userService.getZoneId();
        String dt = ZonedDateTime.now(zoneId).toString();  //null
        return "Hello, it is " + dt;
    }
}
