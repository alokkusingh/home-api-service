package com.alok.home.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class DataSourceMonitor {

    //@Around("execution(* javax.sql.DataSource.getConnection(...))")
    @Around("execution(* com.zaxxer.hikari.HikariDataSource.getConnection(...))")
    public Object getConnection(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        System.out.println("Connection time: " + (endTime - startTime));

        return result;

    }
}

