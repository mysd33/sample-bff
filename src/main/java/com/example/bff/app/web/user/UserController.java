package com.example.bff.app.web.user;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenCheck;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenType;

import com.example.bff.app.web.user.UserForm.GroupOrder;
import com.example.bff.domain.message.MessageIds;
import com.example.bff.domain.model.User;
import com.example.bff.domain.reports.ReportFile;
import com.example.bff.domain.reports.UserListReportCreator;
import com.example.bff.domain.reports.UserListReportData;
import com.example.bff.domain.reports.UserListReportItem;
import com.example.bff.domain.service.user.UserService;
import com.example.fw.common.exception.BusinessException;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.ResultMessage;
import com.example.fw.common.message.ResultMessageType;
import com.example.fw.web.io.ResponseUtil;
import com.example.fw.web.view.CsvDownloadView;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ユーザ管理機能のコントローラクラス
 *
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@TransactionTokenCheck("userTransactionToken")
public class UserController {
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);
    private final PasswordComparisonValidator passwordComparisonValidator;
    private final UserService userService;
    private final UserListReportCreator userListReportCreator;
    private final UserMapper userMapper;

    @InitBinder("userForm")
    public void initBinder(WebDataBinder binder) {
        // 相関項目チェックのValidatorを登録
        binder.addValidators(passwordComparisonValidator);
    }

    /**
     * ユーザー登録画面のGETメソッド用処理.
     */
    @GetMapping("/user")
    @TransactionTokenCheck(type = TransactionTokenType.BEGIN)
    public String displayResistWindow(@ModelAttribute UserForm form, Model model) {
        return "user/regist";
    }

    /**
     * ユーザー登録画面のPOSTメソッド用処理.
     */
    @PostMapping("/user")
    @TransactionTokenCheck()
    public String postUserResist(@ModelAttribute @Validated(GroupOrder.class) UserForm form,
            BindingResult bindingResult, Model model, RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            return "user/regist";
        }
        User user = userMapper.formToModel(form);

        // ユーザー登録処理
        try {
            boolean result = userService.insert(user);
            if (result) {
                appLogger.debug("insert成功");
                attributes.addFlashAttribute(ResultMessage.builder().type(ResultMessageType.INFO)
                        .code(MessageIds.I_EX_0005).args(new String[] { user.getUserId() }).build());
            } else {
                appLogger.debug("insert失敗");
            }
        } catch (BusinessException e) {
            model.addAttribute(e.getResultMessage());
            return "user/regist";
        }

        return "redirect:/userList";
    }

    /**
     * ユーザー一覧画面のGETメソッド用処理.
     */
    @GetMapping("/userList")
    public String getUserList(Model model, Pageable pageable, HttpServletRequest request) {
        // ユーザー一覧の生成
        Page<User> userPage = userService.findAllForPagination(pageable);

        model.addAttribute("requestURI", request.getRequestURI());

        // Modelにユーザーリストを登録
        model.addAttribute("userPage", userPage);
        return "user/userList";
    }

    /**
     * ユーザー詳細画面のGETメソッド用処理.
     */
    @GetMapping("/userDetail/{id:.+}")
    public String getUserDetail(@ModelAttribute UserForm form, Model model, @PathVariable("id") String userId) {

        appLogger.debug("userId = " + userId);
        // ユーザーIDのチェック
        if (userId != null && !userId.isEmpty()) {
            // ユーザー情報を取得
            User user = userService.findOne(userId);
            UserForm newForm = userMapper.modelToForm(user);
            newForm.setPassword("");
            model.addAttribute("userForm", newForm);
        }

        return "user/userDetail";
    }

    /**
     * ユーザー更新用処理.
     */
    @PostMapping(value = "/userDetail", params = "update")
    public String postUserDetailUpdate(@ModelAttribute @Validated(GroupOrder.class) UserForm form,
            BindingResult bindingResult, Model model, RedirectAttributes attributes) {

        appLogger.debug("更新ボタンの処理");
        if (bindingResult.hasErrors()) {
            return "user/userDetail";
        }

        User user = userMapper.formToModel(form);
        try {
            // 更新実行
            userService.updateOne(user);
            attributes.addFlashAttribute(ResultMessage.builder().type(ResultMessageType.INFO).code(MessageIds.I_EX_0006)
                    .args(new String[] { user.getUserId() }).build());
        } catch (BusinessException e) {
            model.addAttribute(e.getResultMessage());
            return "user/userDetail";
        }

        // ユーザー一覧画面を表示
        return "redirect:/userList";
    }

    /**
     * ユーザー削除用処理.
     */
    @PostMapping(value = "/userDetail", params = "delete")
    public String postUserDetailDelete(@ModelAttribute UserForm form, Model model, RedirectAttributes attributes) {
        appLogger.debug("削除ボタンの処理");
        // 削除実行
        try {
            userService.deleteOne(form.getUserId());
            attributes.addFlashAttribute(ResultMessage.builder().type(ResultMessageType.INFO).code(MessageIds.I_EX_0007)
                    .args(new String[] { form.getUserId() }).build());
        } catch (BusinessException e) {
            model.addAttribute(e.getResultMessage());
            return "user/userDetail";
        }
        // ユーザー一覧画面を表示
        return "redirect:/userList";
    }

    /**
     * ユーザー一覧のCSV出力用処理.
     */
    @GetMapping("/userList/csv")
    public ModelAndView getUserListCsv(Model model) {
        String filename = "userList.csv";
        List<User> users = userService.findAll();

        List<UserCsv> csvList = userMapper.modelsToCsvs(users);

        CsvDownloadView view = new CsvDownloadView(UserCsv.class, csvList, filename);
        return new ModelAndView(view);

    }

    /**
     * ユーザー一覧のPDF出力用処理.
     *
     * @param model
     * @return PDFファイルのレスポンス
     */
    @GetMapping("/userList/pdf")
    public ResponseEntity<Resource> getUserListPdf(Model model) {
        List<User> users = userService.findAll();
        List<UserListReportItem> reportItems = userMapper.modelsToReportItems(users);
        ReportFile reportFile = userListReportCreator.createUserListReport(new UserListReportData(reportItems));
        return ResponseUtil.createResponseForPDF(reportFile.getInputStream(), reportFile.getFileName(),
                reportFile.getFileSize());
    }
}
