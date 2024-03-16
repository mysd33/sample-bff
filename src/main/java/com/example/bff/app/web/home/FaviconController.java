package com.example.bff.app.web.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * アプリケーションにファビコンが不要な場合
 * ブラウザが、favicon.icoにアクセスして、404 Not Found エラー等が出ないようにするための
 * Controllerクラス
 */
@Controller
public class FaviconController {

    @GetMapping("favicon.ico")
    @ResponseBody
    public void returnNoFavicon() {      
        // 何もしない
    }
}
