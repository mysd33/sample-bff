package com.example.bff.app.web.common.advice;

import org.springframework.web.bind.annotation.ControllerAdvice;

import com.example.bff.app.web.WebPackage;
import com.example.bff.domain.message.MessageIds;
import com.example.fw.web.advice.AbstractGlobalControllerAdvice;

/**
 * 
 * 集約例外ハンドリングのためのControllerAdviceクラス
 *
 */
@ControllerAdvice(basePackageClasses = { WebPackage.class })
public class GlobalControllerAdvice extends AbstractGlobalControllerAdvice {
    public GlobalControllerAdvice() {
        super(MessageIds.E_EX_9001, MessageIds.W_EX_8006);
    }
}
