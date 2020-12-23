package com.jacky.config;

import com.jacky.domain.AbstractEntity;
import com.jacky.domain.ORMUser;
import com.jacky.orm.DbTemplate;
import com.jacky.service.ORMUserService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author jacky
 * @time 2020-12-23 10:26
 * @discription My ORM
 */

@EnableTransactionManagement
@PropertySource("jdbc.properties")
@Configuration
@ComponentScan(value = "com.jacky")
public class MyORMConfig {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(MyORMConfig.class);
        ORMUserService ormUserService = context.getBean(ORMUserService.class);
        if (ormUserService.fetchUserByEmail("bob@example.com") == null) {
            ORMUser bob = ormUserService.register("bob@example.com", "bob123", "Bob");
            System.out.println("Registered ok: " + bob);
        }
        if (ormUserService.fetchUserByEmail("alice@example.com") == null) {
            ORMUser alice = ormUserService.register("alice@example.com", "helloalice", "Alice");
            System.out.println("Registered ok: " + alice);
        }
        if (ormUserService.fetchUserByEmail("tom@example.com") == null) {
            ORMUser tom = ormUserService.register("tom@example.com", "tomcat", "Alice");
            System.out.println("Registered ok: " + tom);
        }
        // 查询所有用户:
        for (ORMUser u : ormUserService.getUsers(1)) {
            System.out.println(u);
        }
        System.out.println("login...");
        ORMUser tom = ormUserService.login("tom@example.com", "tomcat");
        System.out.println(tom);
        System.out.println(ormUserService.getNameByEmail("alice@example.com"));
        ((ConfigurableApplicationContext) context).close();
    }

    @Bean
    DataSource createDataSource(
            // JDBC URL:
            @Value("${jdbc.url}") String jdbcUrl,
            // JDBC username:
            @Value("${jdbc.username}") String jdbcUsername,
            // JDBC password:
            @Value("${jdbc.password}") String jdbcPassword) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcUsername);
        config.setPassword(jdbcPassword);
        config.addDataSourceProperty("autoCommit", "false");
        config.addDataSourceProperty("connectionTimeout", "5");
        config.addDataSourceProperty("idleTimeout", "60");
        return new HikariDataSource(config);
    }

    @Bean
    JdbcTemplate createJdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    DbTemplate createDbTemplate(@Autowired JdbcTemplate jdbcTemplate) {
        return new DbTemplate(jdbcTemplate, "com.jacky.domain");
    }

    @Bean
    PlatformTransactionManager createTxManager(@Autowired DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

