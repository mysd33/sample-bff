package com.example.bff.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
	//TODO: 一旦メニュー画面へ遷移
	@GetMapping("/")
    public String home() {
        return "redirect:/menu";
    }
}
