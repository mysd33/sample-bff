package com.example.bff.app.web.user;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.example.bff.domain.message.MessageIds;

/**
 * パスワード比較を行う相関項目チェックのValidatorクラス
 */
@Component
public class PasswordComparisonValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return UserForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserForm form = (UserForm) target;
        String password = form.getPassword();
        if (!StringUtils.hasLength(password)) {
            // パスワードが未入力の場合は、エラーとしない
            return;
        }
        String confirmPassword = form.getConfirmPassword();
        // パスワードとパスワード確認用が一致するかチェック
        if (!password.equals(confirmPassword)) {
            errors.rejectValue("confirmPassword", MessageIds.W_EX_5006);
        }
    }
}
