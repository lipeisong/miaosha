package com.itheima.miaosha.validator;

import com.itheima.miaosha.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {

    private boolean required=false;
    //初始化方法可以拿到这个注解
    @Override
    public void initialize(IsMobile isMobile) {
         required = isMobile.required();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        //如果这个值是必须的
        if(required){

            return ValidatorUtil.isMobile(s);
        }else {
            //如果不是必须的
            if(StringUtils.isEmpty(s)){
                return true;
            }else {
                return ValidatorUtil.isMobile(s);
            }
        }

    }
}
