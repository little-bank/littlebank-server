package com.littlebank.finance.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminViewController {
    @GetMapping({"/dashboard", "/transactions", "/reports", "/settings", "charge-history", "refund-history"})
    public String allPages() {
        return "bankwidraw";
    }

    @GetMapping("/admin-login")
    public String login() {
        return "admin-login";
    }
}
