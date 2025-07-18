package com.example.bff.app.web.common.advice;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.terasoluna.gfw.web.token.transaction.InvalidTransactionTokenException;

import com.example.bff.app.web.WebPackage;
import com.example.bff.domain.message.MessageIds;
import com.example.fw.common.exception.SystemException;
import com.example.fw.common.message.ResultMessage;
import com.example.fw.common.message.ResultMessageType;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 
 * 集約例外ハンドリングのためのControllerAdviceクラス
 *
 */
@ControllerAdvice(basePackageClasses = { WebPackage.class })
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    /**
     * 予期せぬ例外発生時のデフォルトのメッセージID
     */
    @Setter
    private String defaultExceptionMessageId = MessageIds.E_EX_9001;

    /**
     * トランザクショントークンチェックエラー時のメッセージID
     */
    @Setter
    private String invalidTransactionTokenExceptionMessageId = MessageIds.W_EX_8006;

    /**
     * エラーページ表示用にModelに格納されるHTTPステータスの属性名
     */
    @Setter
    private String statusModelAttributeName = "status";
    /**
     * エラーページ名
     */
    @Setter
    private String errorPageName = "error";

    /**
     * トランザクショントークンチェックエラー時の処理
     * 
     * @param e     例外
     * @param model Model
     * @return 遷移先エラーページ
     */
    @ExceptionHandler(InvalidTransactionTokenException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String exceptionHandler(final InvalidTransactionTokenException e, final Model model) {
        // 例外クラスのメッセージをModelに登録
        model.addAttribute(ResultMessage.builder().type(ResultMessageType.WARN)
                .code(invalidTransactionTokenExceptionMessageId).build());
        // HTTPのエラーコード（400）をModelに登録
        model.addAttribute(statusModelAttributeName, HttpStatus.BAD_REQUEST);

        return errorPageName;
    }

    /**
     * システム例外時の処理
     * 
     * @param e     例外
     * @param model Model
     * @return 遷移先エラーページ
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String systemExceptionHandler(final SystemException e, final Model model) {

        // 例外クラスのメッセージをModelに登録
        model.addAttribute(ResultMessage.builder().type(ResultMessageType.ERROR).code(e.getCode()).build());
        // HTTPのエラーコード（500）をModelに登録
        model.addAttribute(statusModelAttributeName, HttpStatus.INTERNAL_SERVER_ERROR);

        return errorPageName;
    }

    /**
     * 予期せぬ例外時の処理
     * 
     * @param e     例外
     * @param model Model
     * @return 遷移先エラーページ
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String exceptionHandler(final Exception e, final Model model) {
        // 例外クラスのメッセージをModelに登録
        model.addAttribute(
                ResultMessage.builder().type(ResultMessageType.ERROR).code(defaultExceptionMessageId).build());
        // HTTPのエラーコード（500）をModelに登録
        model.addAttribute(statusModelAttributeName, HttpStatus.INTERNAL_SERVER_ERROR);

        return errorPageName;
    }
}
