package com.byteme;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UIController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Welcome to ByteMe Stock Trader!");
        return "index";
    }
}
