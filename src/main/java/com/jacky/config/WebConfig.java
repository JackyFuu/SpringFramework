package com.jacky.config;

import com.jacky.service.AppService;
import com.jacky.service.MailService;
import com.jacky.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

import java.time.ZoneId;

/**
 * @author jacky
 * @time 2020-12-06 12:47
 * @discription
 */

@Configuration
//根据定义的扫描路径，把符合扫描规则的类装配到spring容器中
@ComponentScan(value = "com.jacky")
//先使用@PropertySource读取配置文件，然后通过@Value以${key:defaultValue}的形式注入，可以极大地简化读取配置的麻烦。
@PropertySource("app.properties")  //自动读取配置文件，然后可以使用@Value正常注入
//IoC容器会自动查找带有@Aspect的Bean，然后根据每个方法的@Before、@Around等注解把AOP注入到特定的Bean中。
@EnableAspectJAutoProxy
public class WebConfig {

    //也可以把@Value("${app.zone:Z}")注入注解写到方法参数中。
    @Value("${app.zone:Z}")
    String zoneId;

    public static void main(String[] args) {
        /**
         * 创建Spring容器，Spring容器就是一个ApplicationContext，它是一个接口，有很多实现类。
         */
        ApplicationContext context = new AnnotationConfigApplicationContext(WebConfig.class);
        AppService appService = context.getBean(AppService.class);
        MailService mailService = context.getBean(MailService.class);
        //测试：对一种类型的Bean，容器只创建一个实例。
        context.getBean(ZoneId.class);
        mailService.sendWelcomeMail();
        appService.printLogo();
        UserService userService = context.getBean(UserService.class);
        userService.register("test@example.com", "password", "test");
        userService.login("bob@example.com", "password");
        //getClass():Returns the runtime class of this Object.
        System.out.println(userService.getClass().getName());
    }

    /**
     * 创建第三方Bean
     * Spring对标记为@Bean的方法只调用一次，因此返回的Bean仍然是单例。
     *
     * 默认情况下，对一种类型的Bean，容器只创建一个实例。
     * 可以用@Bean("name")指定别名，也可以用@Bean+@Qualifier("name")指定别名。
     */
    /**
     * 创建某个Bean时，Spring容器可以根据注解@Profile来决定是否创建。
     * 在运行程序时，加上JVM参数-Dspring.profiles.active=test就可以指定以test环境启动。
     *
     * 实际上，Spring允许指定多个Profile
     * -Dspring.profiles.active=test,master
     * 可以表示test环境，并使用master分支代码。
     */
    @Bean
    @Qualifier("Z")
    @Profile("!test")
    ZoneId createZoneOfZ() {
        return ZoneId.of(zoneId);
    }

    @Bean("utc8")
    @Primary  //在注入时，如果没有指出Bean的名字，Spring会注入标记有@Primary的Bean。
    @Profile({"test", "master"})  //同时满足test&master
    ZoneId createZoneOfUTF8(){
        return ZoneId.of("UTC+08:00");
    }
}
