package com.jacky.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author jacky
 * @time 2020-12-22 19:02
 * @discription 声明式事务
 *                  Spring提供的声明式事务极大地方便了在数据库中使用事务，正确使用声明式事务的关键在于确定好事务边界，理解事务传播级别。
 *                  Spring提供了一个PlatformTransactionManager来表示事务管理器，所有的事务都由它负责管理。而事务由TransactionStatus表示。
 *
 *                  Spring为啥要抽象出PlatformTransactionManager和TransactionStatus？
 *                  原因是JavaEE除了提供JDBC事务外，它还支持分布式事务JTA（Java Transaction API）。
 *                  分布式事务是指多个数据源（比如多个数据库，多个消息系统）要在分布式环境下实现事务的时候，应该怎么实现。
 *                  分布式事务实现起来非常复杂，简单地说就是通过一个分布式事务管理器实现两阶段提交，但本身数据库事务就不快，
 *                  基于数据库事务实现的分布式事务就慢得难以忍受，所以使用率不高。
 *
 *                  Spring为了同时支持JDBC和JTA两种事务模型，就抽象出PlatformTransactionManager。
 *
 *                  使用编程的方式使用Spring事务仍然比较繁琐，更好的方式是通过声明式事务来实现。
 *                  1）使用声明式事务非常简单，除了在DeclarativeTransactionConfig中追加一个上述定义的PlatformTransactionManager外，
 *                  再加一个@EnableTransactionManagement就可以启用声明式事务
 *                  2）然后，对需要事务支持的方法，加一个@Transactional注解：
 *                  3）或者更简单一点，直接在Bean的class处加上，表示所有public方法都具有事务支持：
 *
 *                  Spring对一个声明式事务的方法，如何开启事务支持？
 *                      原理仍然是AOP代理，即通过自动创建Bean的Proxy实现。
 *
 *                  注意：声明了@EnableTransactionManagement后，不必额外添加@EnableAspectJAutoProxy。
 *
 *                  回滚事务：
 *                  1)    默认情况下，如果发生了RuntimeException，Spring的声明式事务将自动回滚。
 *                      在一个事务方法中，如果程序判断需要回滚事务，只需要抛出RuntimeException
 *                  2)  如果要针对Checked Exception回滚事务，需要在@Transactional注解中写出来：
 *                          @Transactional(rollbackFor = {RuntimeException.class, IOException.class})
 *                  3)  为了简化代码，我们强烈建议业务异常体系从RuntimeException派生，
 *                          这样就不必声明任何特殊异常即可让Spring的声明式事务正常工作.
 *
 *                  事务边界：
 *                  事务传播：
 *                  1)Spring的声明式事务为事务传播定义了几个级别，默认传播级别是REQUIRED，
 *                      如果当前没有事务，就创建一个新事务，如果当前有事务，就加入到当前事务中执行。
 *
 *                  2)SUPPORTS：表示如果有事务，就加入到当前事务，如果没有，那也不开启事务执行。
 *                                  这种传播级别可用于查询方法，因为SELECT语句既可以在事务内执行，也可以不需要事务；
 *                  3)MANDATORY：表示必须要存在当前事务并加入执行，否则将抛出异常。
 *                                  这种传播级别可用于核心更新逻辑，比如用户余额变更，它总是被其他事务方法调用，不能直接由非事务方法调用；
 *                  4)REQUIRES_NEW: 表示不管当前有没有事务，都必须开启一个新的事务执行。
 *                                      如果当前已经有事务，那么当前事务会挂起，等新事务完成后，再恢复执行；
 *                  5)NOT_SUPPORTED: 表示不支持事务，如果当前有事务，那么当前事务会挂起，等这个方法执行完成后，再恢复执行；
 *                  6)NEVER: 和NOT_SUPPORTED相比，它不但不支持事务，而且在监测到当前有事务时，会抛出异常拒绝执行；
 *                  7)NESTED: 表示如果当前有事务，则开启一个嵌套级别事务，如果当前没有事务，则开启一个新事务。
 *
 *                  上面这么多种事务的传播级别，其实默认的REQUIRED已经满足绝大部分需求，
 *                  SUPPORTS和REQUIRES_NEW在少数情况下会用到，其他基本不会用到，因为把事务搞得越复杂，不仅逻辑跟着复杂，而且速度也会越慢。
 *
 *                  定义事务的传播级别也是写在@Transactional注解里的：
 *                  @Transactional(propagation = Propagation.REQUIRES_NEW)
 *
 *
 *
 *                  Spring使用声明式事务，最终也是通过执行JDBC事务来实现功能的，那么，一个事务方法，如何获知当前是否存在事务？
 *                      答案是使用ThreadLocal。Spring总是把JDBC相关的Connection和TransactionStatus实例绑定到ThreadLocal。
 *                      如果一个事务方法从ThreadLocal未取到事务，那么它会打开一个新的JDBC连接，同时开启一个新的事务，
 *                      否则，它就直接使用从ThreadLocal获取的JDBC连接以及TransactionStatus。
 *                      因此，事务能正确传播的前提是，方法调用是在一个线程内才行。
 *
 *                      换句话说，事务只能在当前线程传播，无法跨线程传播。
 *
 *                      那如果我们想实现跨线程传播事务呢？
 *                      原理很简单，就是要想办法把当前线程绑定到ThreadLocal的Connection和TransactionStatus实例传递给新线程，
 *                      但实现起来非常复杂，根据异常回滚更加复杂，不推荐自己去实现。
 */

@Component
@ComponentScan(value = "com.jacky")
@PropertySource("jdbc.properties")
@EnableTransactionManagement  //启动声明式事务：声明了@EnableTransactionManagement后，不必额外添加@EnableAspectJAutoProxy。
public class DeclarativeTransactionConfig {

    /**
     * 因为我们的代码只需要JDBC事务，因此，DeclarativeTransactionConfig，
     * 需要再定义一个PlatformTransactionManager对应的Bean，它的实际类型是DataSourceTransactionManager。
     * @param dataSource
     * @return
     */
    @Bean
    PlatformTransactionManager createTxManager(@Autowired DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }


}
