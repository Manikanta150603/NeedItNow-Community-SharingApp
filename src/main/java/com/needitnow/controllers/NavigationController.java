package com.needitnow.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import com.needitnow.entity.User;

@Controller
public class NavigationController {
    
    @GetMapping("/")
    public String home(@RequestParam(required = false) String error,
                      @RequestParam(required = false) Boolean registered,
                      @RequestParam(required = false) String logout,
                      Model model) {
        if (Boolean.TRUE.equals(registered)) {
            model.addAttribute("successMessage", "Registration completed successfully! Please login with your credentials.");
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password");
        }
        return "index";
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "index";
    }
    
    @GetMapping("/user/join-community")
    public String joinCommunity(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }
        model.addAttribute("user", user);
        return "join-community";
    }
}
