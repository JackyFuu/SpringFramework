package com.jacky.condition;


import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author jacky
 * @time 2020-12-22 08:54
 * @discription 根据@Conditional决定是否创建某个Bean
 *
 *  Spring只提供了@Conditional注解，具体判断逻辑还需要我们自己实现。
 *  SpringBoot提供了更多使用起来更简单的条件注解。
 *      @ConditionalOnProperty
 *      @ConditionalOnClass
 */
public class OnSmtpEnvCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        //Gets the value of the specified environment variable.
        return "true".equalsIgnoreCase(System.getenv("smtp"));
    }
}
