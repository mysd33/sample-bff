package com.example.bff.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
	//ページネーション理解のサンプルAPとしたため、
	//ページネーション画面へ直接遷移
	@GetMapping("/")
    public String home() {
        return "redirect:/userList";
    }
}
