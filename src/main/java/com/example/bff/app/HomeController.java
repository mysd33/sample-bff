package com.example.bff.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
	//ログイン画面へ遷移
	@GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
}
