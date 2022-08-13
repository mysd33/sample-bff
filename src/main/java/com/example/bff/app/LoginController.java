package com.example.bff.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 
 * ログイン機能のコントローラクラス
 *
 */
@Controller
public class LoginController {
	
	/**
	 * ログイン画面のGETメソッド用処理
	 */
	@GetMapping("/login")
	public String getLogin(Model model) {
		// ログイン画面へ遷移
		return "login/login";
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