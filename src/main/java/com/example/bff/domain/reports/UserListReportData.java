package com.example.bff.domain.reports;

import java.util.List;

import lombok.Value;

@Value
public class UserListReportData {
    private final List<UserListReportItem> userList;
}
