package com.lin.gulimall.product.exception;

import com.lin.common.exception.BizCodeEnum;
import com.lin.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


/**
 * @Description 集中处理异常
 * @Date 2024/5/29 14:50
 * @Author Lin
 * @Version 1.0
 */

//@ResponseBody
//@ControllerAdvice(basePackages = "com.lin.gulimall.product.controller")
@Slf4j
@RestControllerAdvice(basePackages = "com.lin.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现了问题{},异常类型{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            String field = fieldError.getField();
            String defaultMessage = fieldError.getDefaultMessage();
            errorMap.put(field, defaultMessage);
        });
        return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), BizCodeEnum.VAILD_EXCEPTION.getMsg()).put("data", errorMap);
    }

    // 如果不能准确处理handleValidException中出现的异常，那么就用这个可以处理所有异常的来处理，最终返回R.error()给客户端
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        log.error("未知异常{},未知类型{}", throwable.getMessage(), throwable.getClass());
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.VAILD_EXCEPTION.getMsg());
    }
}
