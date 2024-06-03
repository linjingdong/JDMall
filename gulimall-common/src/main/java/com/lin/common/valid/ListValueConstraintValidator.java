package com.lin.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description TODO
 * @Date 2024/5/30 15:36
 * @Author Lin
 * @Version 1.0
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {
    Set<Integer> set = new HashSet<>();

    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();

        for (int val : vals) {
            set.add(val);
        }
    }

    /**
     * @param integer:需要校验的值
     * @param constraintValidatorContext:{String}
     * @return boolean
     */
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {

        return set.contains(integer);
    }
}
