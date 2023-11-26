package com.peoplein.moiming.annotation.validator;

import com.peoplein.moiming.annotation.EnumConstraint;
import com.peoplein.moiming.domain.enums.PolicyType;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class EnumValuesValidator implements ConstraintValidator<EnumConstraint, PolicyType> {

    private List<PolicyType> allowedValues;

    @Override
    public void initialize(EnumConstraint constraint) {
        allowedValues = Arrays.asList(constraint.allowedValues());
    }

    @Override
    public boolean isValid(PolicyType value, ConstraintValidatorContext context) {
        return allowedValues.contains(value);
    }
}