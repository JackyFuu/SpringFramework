package com.jacky.config;

import com.jacky.domain.User;
import com.jacky.service.UserService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author jacky
 * @time 2020-12-22 15:51
 * @discription JDBCTemplate
 *              JdbcTemplate只是对JDBC操作的一个简单封装，它的目的是尽量减少手动编写try(resource) {...}的代码，
 *              对于查询，主要通过RowMapper实现了JDBC结果集到Java对象的转换。
 *
 *              JdbcTemplate的用法，那就是：
 *                  1)针对简单查询，优选query()和queryForObject()，因为只需提供SQL语句、参数和RowMapper；
 *                  2)针对更新操作，优选update()，因为只需提供SQL语句和参数；
 *                  3)任何复杂的操作，最终也可以通过execute(ConnectionCallback)实现，因为拿到Connection就可以做任何JDBC操作。
*
 *              实际上我们使用最多的仍然是各种查询。如果在设计表结构的时候，能够和JavaBean的属性一一对应，那么直接使用BeanPropertyRowMapper就很方便。
 */

@ComponentScan(value = "com.jacky")
@Configuration
//通过@PropertySource("jdbc.properties")读取数据库配置文件；
@PropertySource("jdbc.properties")
public class JdbcConfig {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(JdbcConfig.class);
        UserService userService = context.getBean(UserService.class);
        userService.register("jackyfu@example.com", "password1", "jackyfu");
        userService.register("jackyfu1@example.com", "password2", "jackyfu1");
        //User bob = userService.getUserByName("Bob");
        //System.out.println(bob);
        User tom = userService.register("tom1@example.com", "password3", "Tom1");
        System.out.println(tom);
        System.out.println("Total: " + userService.getUsers());
        for (User u : userService.getUsers(1)) {
            System.out.println(u);
        }
        ((ConfigurableApplicationContext) context).close();
    }

    //通过@Value("${jdbc.url}")注入配置文件的相关配置；
    @Value("${jdbc.url:jdbc:hsqldb:file:testdb}")
    String jdbcUrl;

    @Value("${jdbc.username:sa}")
    String jdbcUsername;

    @Value("${jdbc.password:}")
    String jdbcPassword;

    @Autowired
    DataSource dataSource;

    //创建一个JdbcTemplate实例，它需要注入DataSource，这是通过方法参数完成注入的。
    @Bean
    JdbcTemplate createJdbcTemplate(@Autowired DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }

    //创建一个DataSource实例，它的实际类型是HikariDataSource，创建时需要用到注入的配置；
    @Bean
    DataSource createDataSource(){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcUsername);
        config.setPassword(jdbcPassword);
        config.addDataSourceProperty("autoCommit", "true");
        config.addDataSourceProperty("connectionTimeout", "5");
        config.addDataSourceProperty("idleTimeout", "60");
        return new HikariDataSource(config);
    }
}
