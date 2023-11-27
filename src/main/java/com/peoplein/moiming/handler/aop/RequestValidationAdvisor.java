package com.peoplein.moiming.handler.aop;

import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingValidationException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class RequestValidationAdvisor {

    /*
    POST 요청 내 Body 데이터가 있을 시 사용되는 Validation AOP
    */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {
    }


    /*
    Patch 요청 내 Body 데이터가 있을 시 사용되는 Validation AOP
    */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void patchMapping() {
    }


    // JoinPoint, 전후 제어 // @After, @Before 는 각각 하나
    @Around("postMapping() || patchMapping()")
    public Object catchValidationError(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();// 해당 AOP 대상 메소드로 진입하는 PARAMS 들을 받아온다
        for (Object arg : args) {
            if (arg instanceof BindingResult) { // 들어오는 Validation 의 결과 중 Error 가 있는지 확인한다

                BindingResult br = (BindingResult) arg;

                if (br.hasErrors()) {
                    Map<String, String> errMap = new HashMap<>(); // 필드별로 발생한 에러 (BR 의 이유) 를 Exception 으로 전달하여, 바디로 전달될 수 있도록 세팅해준다
                    for (FieldError fe : br.getFieldErrors()) {
                        errMap.put(fe.getField(), fe.getDefaultMessage());
                    }

                    throw new MoimingValidationException(ExceptionValue.COMMON_REQUEST_VALIDATION, errMap);
                }
            }
        }

        return proceedingJoinPoint.proceed();
    }
}