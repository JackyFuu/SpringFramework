package com.jacky.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @author jacky
 * @time 2020-12-22 09:43
 * @discription AOP是Aspect Oriented Programming，即面向切面编程。
 *
 *      在Java平台上，对于AOP的织入，有3种方式：
 *          编译期：在编译时，由编译器把切面调用编译进字节码，这种方式需要定义新的关键字并扩展编译器，
 *              AspectJ就扩展了Java编译器，使用关键字aspect来实现织入；
 *          类加载器：在目标类被装载到JVM时，通过一个特殊的类加载器，对目标类的字节码重新“增强”；
 *          运行期：目标对象和切面都是普通Java类，通过JVM的动态代理功能或者第三方库实现运行期动态织入。
 *
 *      AOP技术看上去比较神秘，但实际上，它本质就是一个动态代理，让我们把一些常用功能如权限检查、日志、事务等，从每个业务方法中剥离出来。
 *
 *      1、Spring对接口类型使用JDK动态代理，对普通类使用CGLIB创建子类。如果一个Bean的class是final，Spring将无法为其创建子类。
 *      2、虽然Spring容器内部实现AOP的逻辑比较复杂（需要使用AspectJ解析注解，并通过CGLIB实现代理类），
 *          但使用AOP非常简单，其步骤如下：
 *          1）定义执行方法，并在方法上通过AspectJ的注解告诉Spring应该在何处调用此方法；
 *          2）标记@Component和@Aspect
 *          3) 在@Configuration类上标注@EnableAspectJAutoProxy
 *
 *      3、拦截器类型
 *          @Before         先执行拦截代码，再执行目标代码。如果拦截器抛异常，那么目标代码就不执行了；
 *          @After          先执行目标代码，再执行拦截器代码。无论目标代码是否抛异常，拦截器代码都会执行；
 *          @AfterRetruning 和@After不同的是，只有当目标代码正常返回时，才执行拦截器代码；
 *          @AfterThrowing  和@After不同的是，只有当目标代码抛出了异常时，才执行拦截器代码；
 *          @Around         能完全控制目标代码是否执行，并可以在执行前后、抛异常后执行任意拦截代码，可以说是包含了上面所有功能。
 */

@Aspect
@Component
public class LoggingAspect {

    //执行UserService的每个public方法前执行doAccessCheck()代码。
    @Before("execution(public * com.jacky.service.UserService.*(..))")
    public void doAccessCheck(){
        System.out.println("[Before] do access check...");
    }

    //在执行MailService的每个方法前后执行

    /**
     * 与@Before不同，@Around可以决定是否执行目标方法，
     * 因此，在doLogging()内部先打印日志，再调用方法，最后打印日志后返回结果。
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("execution(public * com.jacky.service.MailService.*(..))")
    public Object doLogging(ProceedingJoinPoint joinPoint) throws Throwable{
        System.err.println("[Around] start " + joinPoint.getSignature());
        //Proceed with the next advice or target method invocation
        Object retVal = joinPoint.proceed();
        System.err.println("[Around] done " + joinPoint.getSignature());
        return retVal;
    }
}
