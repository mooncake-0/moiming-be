package com.peoplein.moiming.annotation;

import com.peoplein.moiming.annotation.validator.EnumValuesValidator;
import com.peoplein.moiming.domain.enums.PolicyType;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValuesValidator.class)
@Documented
public @interface EnumConstraint {

    String message() default "유효하지 않은 값입니다";
    PolicyType[] allowedValues() default {};
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
