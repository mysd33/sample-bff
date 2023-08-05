package com.example.bff.app.web.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.bff.app.web.common.authentication.AuthenticationUtil;

@Controller
public class HomeController {

    /**
     * トップページ遷移
     */
    @GetMapping("/")
    public String home() {
        // ログイン済みの場合
        if (AuthenticationUtil.isAuthenticated()) {
            // メニュー画面へ遷移
            return "redirect:/menu";
        }
        
        // ログイン画面へ遷移        
        return "redirect:/login";
    }
}
