package com.example.bff.domain.reports;

import java.util.Date;

import lombok.Value;

@Value
public class UserListReportItem {

    String userId;
    String userName;
    Date birthday;
    String role;
    String note = ""; // 備考欄

    public String getRoleName() {
        return switch (role) {
            case "ROLE_ADMIN" -> "管理者";
            case "ROLE_GENERAL" -> "一般ユーザ";
            default -> "不明";
        };
    }
}
