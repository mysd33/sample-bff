package com.example.fw.web.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.example.fw.web.validation.UploadFileRequired.List;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

/**
 * ファイルが選択されていることを検証するためのValidatorのアノテーション
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Constraint(validatedBy = UploadFileRequiredValidator.class)
@Repeatable(List.class)
//https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#_validation_target_specification_for_purely_composed_constraints
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public @interface UploadFileRequired {
    String message() default "{com.example.fw.web.validation.UploadFileRequired.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        UploadFileRequired[] value();
    }

}