package com.etrs.orderservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(public * com.etrs.orderservice.controller.*Controller.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logExecutionTimeAndParameters(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toString();
        Object[] args = joinPoint.getArgs();

        log.info("AOP Audit -> Entering method: {} with arguments: {}", methodName, Arrays.toString(args));
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed(args);

        if (result instanceof Mono<?> monoResult) {
            return monoResult.doOnSuccess(o -> {
                long elapsedTime = System.currentTimeMillis() - start;
                log.info("AOP Audit -> Finished method: {} in {} ms", methodName, elapsedTime);
            });
        }

        long elapsedTime = System.currentTimeMillis() - start;
        log.info("AOP Audit -> Finished method: {} in {} ms", methodName, elapsedTime);

        return result;
    }
}
