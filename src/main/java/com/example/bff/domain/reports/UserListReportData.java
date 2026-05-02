package com.example.bff.domain.reports;

import java.util.List;

import lombok.Value;

@Value
public class UserListReportData {

    List<UserListReportItem> userList;
}
