package com.example.bff.app.web.user;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenCheck;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenType;

import com.example.bff.app.web.user.UserForm.GroupOrder;
import com.example.bff.domain.model.User;
import com.example.bff.domain.service.user.UserService;
import com.example.fw.common.exception.BusinessException;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
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
    private final UserService userService;
    private final UserMapper userMapper;

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
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "user/regist";
        }
        User user = userMapper.formToModel(form);

        // ユーザー登録処理
        try {
            boolean result = userService.insert(user);
            if (result) {
                appLogger.debug("insert成功");
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
        if (userId != null && userId.length() > 0) {
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
            BindingResult bindingResult, Model model) {

        appLogger.debug("更新ボタンの処理");
        if (bindingResult.hasErrors()) {
            return "user/userDetail";
        }

        User user = userMapper.formToModel(form);

        try {
            // 更新実行
            boolean result = userService.updateOne(user);

            if (result) {
                model.addAttribute("result", "更新成功");
            } else {
                model.addAttribute("result", "更新失敗");
            }

        } catch (DataAccessException e) {
            model.addAttribute("result", "更新失敗");
        }

        // ユーザー一覧画面を表示
        return "redirect:/userList";
    }

    /**
     * ユーザー削除用処理.
     */
    @PostMapping(value = "/userDetail", params = "delete")
    public String postUserDetailDelete(@ModelAttribute UserForm form, Model model) {
        appLogger.debug("削除ボタンの処理");

        // TODO:自分のユーザ情報は削除できないようにする
        // 削除実行
        boolean result = userService.deleteOne(form.getUserId());

        if (result) {
            model.addAttribute("result", "削除成功");
        } else {
            model.addAttribute("result", "削除失敗");
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

}
