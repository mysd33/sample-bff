package com.example.fw.web.validation;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * ファイルのサイズが許容サイズ内であることを検証するためのValidatorの実装 
 *
 */
public class UploadFileMaxSizeValidator implements ConstraintValidator<UploadFileMaxSize, MultipartFile> {

    private UploadFileMaxSize constraint;

    @Override
    public void initialize(UploadFileMaxSize constraint) {
        this.constraint = constraint;
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        if (constraint.value() < 0 || multipartFile == null) {
            return true;
        }
        return multipartFile.getSize() <= constraint.value();
    }

}