package com.company.itoms.aspect;

import com.alibaba.fastjson2.JSON;
import com.company.itoms.annotation.Log;
import com.company.itoms.entity.OperationLogEntity;
import com.company.itoms.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogService operationLogService;

    @Pointcut("@annotation(com.company.itoms.annotation.Log)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        
        Object result = null;
        Throwable error = null;
        
        try {
            // 执行方法
            result = point.proceed();
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            // 执行时长(毫秒)
            long time = System.currentTimeMillis() - beginTime;
            // 保存日志
            saveLog(point, time, result, error);
        }
        
        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long time, Object result, Throwable error) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        OperationLogEntity operationLog = new OperationLogEntity();
        Log logAnnotation = method.getAnnotation(Log.class);
        
        if (logAnnotation != null) {
            // 注解上的描述
            operationLog.setModule(logAnnotation.module());
            operationLog.setType(logAnnotation.type());
            operationLog.setDescription(logAnnotation.description());
        }

        // 请求相关信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            operationLog.setRequestMethod(request.getMethod());
            operationLog.setRequestUrl(request.getRequestURI());
            operationLog.setRequestIp(getIpAddr(request));
        }

        // 请求参数
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                operationLog.setRequestParams(JSON.toJSONString(args[0]));
            }
        } catch (Exception e) {
            log.error("解析请求参数失败", e);
        }

        // 响应结果或错误信息
        if (error != null) {
            operationLog.setStatus(1);
            operationLog.setErrorMsg(error.getMessage());
        } else {
            operationLog.setStatus(0);
            try {
                operationLog.setResponseData(JSON.toJSONString(result));
            } catch (Exception e) {
                log.error("解析响应数据失败", e);
            }
        }

        operationLog.setExecuteTime(time);
        
        // 此处应从当前会话(如Spring Security Context)获取操作人信息
        // 为了演示，这里暂时写死或留空
        operationLog.setOperatorId(1L);
        operationLog.setOperatorName("admin");

        // 保存系统日志
        operationLogService.save(operationLog);
    }

    /**
     * 获取IP地址
     */
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
