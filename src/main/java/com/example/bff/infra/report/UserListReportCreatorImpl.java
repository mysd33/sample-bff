package com.example.bff.infra.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.util.ResourceUtils;

import com.example.bff.domain.reports.ReportFile;
import com.example.bff.domain.reports.UserListReportCreator;
import com.example.bff.domain.reports.UserListReportData;
import com.example.bff.domain.reports.UserListReportItem;
import com.example.fw.common.reports.AbstractJasperReportCreator;
import com.example.fw.common.reports.Report;
import com.example.fw.common.reports.ReportCreator;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * UserListReportCreatorの実装クラス<br>
 * 
 */
// @ReportCreatorを付与し、Bean定義
@ReportCreator(id = "R001", name = "ユーザ一覧")
// AbstractJasperReportCreatorを継承
// 型パラメータに帳票作成に必要なデータの型を指定
public class UserListReportCreatorImpl extends AbstractJasperReportCreator<UserListReportData>
        implements UserListReportCreator {
    private static final String USER_LIST_FILE_NAME = "ユーザ一覧.pdf";
    private static final String JRXML_FILE_PATH = "classpath:reports/userlist-report.jrxml";

    @Override
    public ReportFile createUserListReport(UserListReportData data) throws DataAccessException {
        Report report = createPDFReport(data);
        return ReportFile.builder()//
                .inputStream(report.getInputStream())//
                .fileName(USER_LIST_FILE_NAME)//
                .fileSize(report.getSize())//
                .build();
    }

    @Override
    protected File getMainJRXMLFile() throws FileNotFoundException {
        return ResourceUtils.getFile(JRXML_FILE_PATH);
    }

    @Override
    protected JRDataSource getDataSource(UserListReportData data) {
        List<UserListReportItem> userList = data.getUserList();
        // JRBeanCollectionDataSourceを使用して、Beanのコレクションをデータソースに変換
        return new JRBeanCollectionDataSource(userList);
    }

}
