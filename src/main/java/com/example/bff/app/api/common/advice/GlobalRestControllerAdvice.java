package com.example.bff.app.api.common.advice;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.bff.app.api.APIPackage;
import com.example.fw.web.advice.AbstractRestControllerAdvice;
import com.example.fw.web.advice.ErrorResponseCreator;

/**
 * 
 * 集約例外ハンドリングのためのRestControllerAdviceクラス
 *
 */
@RestControllerAdvice(basePackageClasses = { APIPackage.class })
public class GlobalRestControllerAdvice extends AbstractRestControllerAdvice {

    public GlobalRestControllerAdvice(ErrorResponseCreator errorResponseCreator) {
        super(errorResponseCreator);
    }


}
