package com.example.bff.domain.reports;

import org.springframework.dao.DataAccessException;

/**
 * ユーザ一覧の帳票を作成するReportCreatorインターフェース
 * 
 */
public interface UserListReportCreator {
    
    /**
     *  ユーザ一覧の帳票を作成する
     *  @param userList ユーザ一覧のデータ
     *  @return 作成された帳票ファイル
     */
    ReportFile createUserListReport(UserListReportData data) throws DataAccessException;
}
