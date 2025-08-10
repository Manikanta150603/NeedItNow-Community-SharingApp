package com.needitnow.controllers;

import com.needitnow.entity.User;
import com.needitnow.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        try {
            userService.registerUser(user);
            return "redirect:/?registered=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
    
    @PostMapping("/login")
    public String loginUser(@RequestParam String email, 
                           @RequestParam String password,
                           HttpSession session,
                           Model model) {
        try {
            User user = userService.login(email, password);
            session.setAttribute("user", user);
            return "redirect:/user/dashboard";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/api/current-user")
    @ResponseBody
    public Map<String, Object> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            return response;
        }
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "User not logged in");
        return errorResponse;
    }

    private User getUserFromSession(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    private boolean isUserLoggedIn(HttpSession session) {
        return getUserFromSession(session) != null;
    }

    private String redirectToHomeIfNotLoggedIn(HttpSession session) {
        if (!isUserLoggedIn(session)) {
            return "redirect:/";
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        String redirect = redirectToHomeIfNotLoggedIn(session);
        if (redirect != null) {
            return redirect;
        }
        model.addAttribute("user", getUserFromSession(session));
        return "dashboard";
    }
    
    @GetMapping("/api/users/{userId}/contact-info")
    @ResponseBody
    public Map<String, Object> getUserContactInfo(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Map<String, Object> contactInfo = new HashMap<>();
        contactInfo.put("name", user.getFullName() != null ? user.getFullName() : "");
        contactInfo.put("email", user.getEmail());
        contactInfo.put("phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        
        return contactInfo;
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
