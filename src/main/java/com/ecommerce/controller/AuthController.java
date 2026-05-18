package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/auth/local-bypass")
    public String localBypass(HttpSession session, HttpServletRequest request) {
        // Safe check to verify we are only running this on localhost / local development environment
        String serverName = request.getServerName();
        if (!"localhost".equals(serverName) && !"127.0.0.1".equals(serverName)) {
            return "redirect:/login";
        }

        String email = "asmitabiswas220@gmail.com";
        String name = "Asmita Biswas (Admin)";

        User user = userService.findByUsername(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(email);
                    newUser.setName(name);
                    newUser.setPassword("LOCAL_BYPASS");
                    return userService.save(newUser);
                });

        session.setAttribute("loggedInUser", user);
        return "redirect:/?loginSuccess=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User updatedUser, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        user.setName(updatedUser.getName());
        user.setUsername(updatedUser.getUsername());
        user.setPhoneNumber(updatedUser.getPhoneNumber());

        userService.save(user);

        session.setAttribute("loggedInUser", user);
        return "redirect:/profile?success=true";
    }
}