package com.jacky.config;

import com.jacky.domain.MybatisUser;
import com.jacky.service.MybatisUserService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author jacky
 * @time 2020-12-23 08:49
 * @discription  Spring集成MyBatis
 *                  使用Hibernate或JPA操作数据库时，这类ORM干的主要工作就是把ResultSet的每一行变成Java Bean，
 *                  或者把Java Bean自动转换到INSERT或UPDATE语句的参数中，从而实现ORM。
 *
 *                  而ORM框架之所以知道如何把行数据映射到Java Bean，是因为我们在Java Bean的属性上给了足够的注解作为元数据，
 *                  ORM框架获取Java Bean的注解后，就知道如何进行双向映射。
 *
 *                  那么，ORM框架是如何跟踪Java Bean的修改，以便在update()操作中更新必要的属性？
 *                  答案是使用Proxy模式，从ORM框架读取的User实例实际上并不是User类，而是代理类，代理类继承自User类，但针对每个setter方法做了覆写。
 *                  代理类可以跟踪到每个属性的变化。
 *
 *                  ORM框架通常提供了缓存，并且还分为一级缓存和二级缓存。
 *                      一级缓存是指在一个Session范围内的缓存，常见的情景是根据主键查询时，两次查询可以返回同一实例；
 *                      二级缓存是指跨Session的缓存，一般默认关闭，需要手动配置。二级缓存极大的增加了数据的不一致性，原因在于SQL非常灵活，常常会导致意外的更新。
 *
 *                  全自动ORM框架：JPA(Java Persistence API)、Hibernate
 *
 *                  对比Spring提供的JdbcTemplate，它和ORM框架相比，主要有几点差别
 *                      查询后需要手动提供Mapper实例以便把ResultSet的每一行变为Java对象；
 *                      增删改操作所需的参数列表，需要手动传入，即把User实例变为[user.id, user.name, user.email]这样的列表，比较麻烦。
 *
 *                      但是JdbcTemplate的优势在于它的确定性：即每次读取操作一定是数据库操作而不是缓存，所执行的SQL是完全确定的，缺点就是代码比较繁琐。
 *
 *                  半自动ORM框架
 *                  所以，介于全自动ORM如Hibernate和手写全部如JdbcTemplate之间，
 *                  还有一种半自动的ORM，它只负责把ResultSet自动映射到Java Bean，或者自动填充Java Bean参数，但仍需自己写出SQL。MyBatis就是这样一种半自动化ORM框架。
 *
 *                  ORM的设计套路都是类似的。使用MyBatis的核心就是创建SqlSessionFactory，这里我们需要创建的是SqlSessionFactoryBean
 *
 *                  MyBatis是一个半自动化的ORM框架，需要手写SQL语句，没有自动加载一对多或多对一关系的功能。
 */


@Configuration
@ComponentScan(value = "com.jacky")
@EnableTransactionManagement
@PropertySource("jdbc.properties")
@MapperScan("com.jacky.mapper")   //MyBatis自动扫描指定包的所有Mapper并创建实现类
public class MyBatisConfig {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(MyBatisConfig.class);
        MybatisUserService mybatisUserService = context.getBean(MybatisUserService.class);
        if (mybatisUserService.fetchUserByEmail("bob@example.com") == null) {
            MybatisUser bob = mybatisUserService.register("bob@example.com", "bob123", "Bob");
            System.out.println("Registered ok: " + bob);
        }
        if (mybatisUserService.fetchUserByEmail("alice@example.com") == null) {
            MybatisUser alice = mybatisUserService.register("alice@example.com", "helloalice", "Alice");
            System.out.println("Registered ok: " + alice);
        }
        if (mybatisUserService.fetchUserByEmail("tom@example.com") == null) {
            MybatisUser tom = mybatisUserService.register("tom@example.com", "tomcat", "Alice");
            System.out.println("Registered ok: " + tom);
        }
        // 查询所有用户:
        for (MybatisUser u : mybatisUserService.getUsers(1)) {
            System.out.println(u);
        }
        System.out.println("login...");
        MybatisUser tom = mybatisUserService.login("tom@example.com", "tomcat");
        System.out.println(tom);
        ((ConfigurableApplicationContext) context).close();
    }

    /**
     * ORM的设计套路都是类似的。使用MyBatis的核心就是创建SqlSessionFactory，这里我们需要创建的是SqlSessionFactoryBean：
     * @param dataSource
     * @return
     */
    @Bean
    SqlSessionFactoryBean createSqlSessionFactoryBean(@Autowired DataSource dataSource){
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean;
    }

    /**
     * 因为Mybatis可以直接使用Spring管理的声明式事务，因此，创建事务管理器和使用JDBC是一样的。
     */
    @Bean
    PlatformTransactionManager createTxManager(@Autowired DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 数据源
     * @param jdbcUrl
     * @param jdbcUsername
     * @param jdbcPassword
     * @return
     */
    @Bean
    DataSource createDataSource(
            // JDBC URL:
            @Value("${jdbc.url}") String jdbcUrl,
            // JDBC username:
            @Value("${jdbc.username}") String jdbcUsername,
            // JDBC password:
            @Value("${jdbc.password}") String jdbcPassword){
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
