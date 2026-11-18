package com.library.config;

import com.fasterxml.jackson.databind.ObjectMapper; // 引入 Spring Boot 自带的 Jackson
import com.library.entity.OperationLog;
import com.library.entity.User;
import com.library.repository.OperationLogRepository;
import com.library.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private OperationLogRepository logRepository;

    // 注入 Spring 内置的 ObjectMapper
    @Autowired
    private ObjectMapper objectMapper;

    // 拦截所有 Controller 中的方法
    @Around("execution(* com.library.controller.*.*(..))")
    public Object recordLog(ProceedingJoinPoint joinPoint) throws Throwable {

        // 放行原方法
        Object result = joinPoint.proceed();

        try {
            // 获取请求上下文
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String token = request.getHeader("Authorization");

                OperationLog log = new OperationLog();
                log.setMethod(request.getMethod() + " " + request.getRequestURI());

                // 获取操作人
                if (token != null && UserService.TOKEN_CACHE.containsKey(token)) {
                    User user = UserService.TOKEN_CACHE.get(token);
                    log.setUsername(user.getName());
                } else {
                    log.setUsername("匿名");
                }

                log.setOperation(joinPoint.getSignature().getName()); // 方法名作为操作

                // 记录参数 (过滤掉文件上传等无法序列化的参数)
                try {
                    log.setParams(objectMapper.writeValueAsString(joinPoint.getArgs()));
                } catch (Exception e) {
                    log.setParams("参数包含文件流或无法解析");
                }

                logRepository.save(log);
            }
        } catch (Exception e) {
            System.err.println("记录日志失败: " + e.getMessage());
        }

        return result;
    }
}