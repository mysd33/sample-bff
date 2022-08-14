package com.example.bff.app;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 
 * ログイン機能のコントローラクラス
 *
 */
@Controller
public class LoginController {

	@ModelAttribute
	public LoginForm setUpForm() {
		LoginForm form = new LoginForm();
		return form;
	}

	/**
	 * ログイン画面のGETメソッド用処理
	 */
	@GetMapping("/login")
	public String getLogin(Model model , HttpSession session) {
		// ログイン画面へ遷移
		return "login/login";
	}

	@PostMapping("/login")
	public String postLogin(@Validated LoginForm form, BindingResult result, HttpSession session) {
		if (result.hasErrors()) {
			return "login/login";
		}
		return "forward:/authenticate";
	}

	/**
	 * ログイン成功後のメニュー画面遷移処理
	 */
	@GetMapping("/menu")
	public String menu(Model model) {
		return "menu/menu";
	}

	/**
	 * 管理者用ユーザ管理ページ遷移用処理
	 */
	@GetMapping("/admin")
	public String admin(Model model) {
		return "redirect:/userList";
	}

}