package com.example.bff.app;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class LoginForm implements Serializable {

    private static final long serialVersionUID = -238883372090238470L;

    @NotBlank
    private String userId; // ユーザーID

    @NotBlank
    private String password; // パスワード
}
