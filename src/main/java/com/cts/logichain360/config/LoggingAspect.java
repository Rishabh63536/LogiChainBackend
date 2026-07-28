package com.cts.logichain360.config;

import org.aspectj.lang.ProceedingJoinPoint;

import org.aspectj.lang.annotation.Around;

import org.aspectj.lang.annotation.Aspect;

import org.aspectj.lang.annotation.Pointcut;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect

@Component

public class LoggingAspect {

	private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

	@Pointcut("within(com.cts.logichain360.controller..*) || within(com.cts.logichain360.service..*)")
	public void applicationPackagePointcut() {

	}

	@Around("applicationPackagePointcut()")
	public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
		String className = joinPoint.getTarget().getClass().getSimpleName();
		String methodName = joinPoint.getSignature().getName();
		Object[] args = joinPoint.getArgs();
		log.info(">> Entering {}.{}() with argument[s] = {}", className, methodName, Arrays.toString(args));

		long startTime = System.currentTimeMillis();

		try {
			Object result = joinPoint.proceed();
			// Calculate execution time
			long elapsedTime = System.currentTimeMillis() - startTime;
			// Log Method Exit
			log.info("<< Exiting {}.{}() with result = {} (Execution time: {} ms)",
					className, methodName, result, elapsedTime);
			return result;
		} catch (IllegalArgumentException e) {
			log.error("Illegal argument: {} in {}.{}()", Arrays.toString(args), className, methodName);
			throw e;
		} catch (Exception e) {
			log.error("Exception in {}.{}() with cause = {}", className, methodName, e.getMessage());
			throw e;
		}
	}

}
