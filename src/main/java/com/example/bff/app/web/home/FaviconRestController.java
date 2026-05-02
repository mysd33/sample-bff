package com.example.bff.app.web.home;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * アプリケーションにファビコンが不要な場合 ブラウザが、favicon.icoにアクセスして、404 Not Found エラー等が出ないようにするための
 * RestControllerクラス
 */
@RestController
public class FaviconRestController {

    @GetMapping("favicon.ico")
    public void returnNoFavicon() {
        // 何もしない
    }
}
