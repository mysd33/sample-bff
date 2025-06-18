package com.example.fw.web.validation;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * ファイルが空でないことを検証するためのValidatorの実装 
 *
 */
public class UploadFileNotEmptyValidator implements ConstraintValidator<UploadFileNotEmpty, MultipartFile> {

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        if (multipartFile == null || !StringUtils.hasLength(multipartFile.getOriginalFilename())) {
            return true;
        }
        return !multipartFile.isEmpty();
    }

}
