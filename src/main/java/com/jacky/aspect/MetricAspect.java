package com.jacky.aspect;

import com.jacky.annotation.MetricTime;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author jacky
 * @time 2020-12-22 10:55
 * @discription 使用注解实现AOP需要先定义注解，然后使用@Around("@annotation(name)")实现装配；
 *              使用注解既简单，又能明确标识AOP装配，是使用AOP推荐的方式。
 *
 *              如果我们自己写的Bean希望在一个数据库事务中被调用，就标注上@Transactional
 *                  可以将@Transactional定义在method上也可以定义在class上
 */

@Aspect
@Component
public class MetricAspect {

    //符合条件的目标方法是带有@MetricTime注解的方法
    @Around("@annotation(metricTime)")
    public Object metric(ProceedingJoinPoint joinPoint, MetricTime metricTime) throws Throwable{
        String name = metricTime.value();
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long t = System.currentTimeMillis() - start;
            //写入日志或发送到JMX：
            System.err.println("[Metrics] " + name + ": " + t + "ms");
        }

    }
}
