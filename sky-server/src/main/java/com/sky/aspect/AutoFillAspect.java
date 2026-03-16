package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut(){}

    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段填充");
        //获取方法签名对象
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        //获取方法上的注解对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);

        //获取当前方法参数
        Object[] args = joinPoint.getArgs();
        Object object = args[0];

        if(args == null || args.length == 0){
            return;
        }
        if(autoFill.value() ==  OperationType.INSERT){
            try {
                //通过反射获取对象的set方法
                Method setCreateTime = object.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                Method setCreateUser = object.getClass().getDeclaredMethod("setCreateUser", Long.class);
                Method setUpdateTime = object.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = object.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                //通过反射用set方法为对象属性赋值
                setCreateTime.invoke(object, LocalDateTime.now());
                setUpdateTime.invoke(object, LocalDateTime.now());
                setCreateUser.invoke(object, BaseContext.getCurrentId());
                setUpdateUser.invoke(object, BaseContext.getCurrentId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (autoFill.value() == OperationType.UPDATE) {
            try {
                Method setUpdateTime = object.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = object.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                setUpdateTime.invoke(object, LocalDateTime.now());
                setUpdateUser.invoke(object, BaseContext.getCurrentId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

}
