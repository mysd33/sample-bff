package com.example.bff.domain.reports;

import java.util.Date;

import lombok.Value;

@Value
public class UserListReportItem {
    private String userId;
    private String userName;
    private Date birthday;
    private String role;
    private String note = ""; // 備考欄
    
    public String getRoleName() {
        switch (role) {
            case "ROLE_ADMIN":
                return "管理者";
            case "ROLE_GENERAL":
                return "一般ユーザ";
            default:
                return "不明";
        }
    }
}
