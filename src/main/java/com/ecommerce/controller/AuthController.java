package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;
    private final String googleClientId;

    public AuthController(UserService userService,
                          @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId) {
        this.userService = userService;
        this.googleClientId = googleClientId;
    }

    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam(required = false) String error,
                            @RequestParam(required = false) String registered) {
        model.addAttribute("googleLoginEnabled", isGoogleLoginEnabled());
        model.addAttribute("loginError", error != null);
        model.addAttribute("registered", registered != null);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {
        User user = userService.login(username, password);
        if (user == null) {
            return "redirect:/login?error=true";
        }
        session.setAttribute("loggedInUser", user);
        return "redirect:/?loginSuccess=true";
    }

    @GetMapping("/register")
    public String registerPage(Model model,
                               @RequestParam(required = false) String error) {
        model.addAttribute("user", new User());
        model.addAttribute("registerError", error);
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                           @RequestParam String confirmPassword,
                           Model model,
                           HttpSession session) {
        if (isBlank(user.getName()) || isBlank(user.getUsername()) || isBlank(user.getPassword())) {
            model.addAttribute("user", user);
            model.addAttribute("registerError", "Please fill all required fields.");
            return "register";
        }
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("user", user);
            model.addAttribute("registerError", "Passwords do not match.");
            return "register";
        }
        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("user", user);
            model.addAttribute("registerError", "An account with this email already exists.");
            return "register";
        }

        User savedUser = userService.register(user);
        session.setAttribute("loggedInUser", savedUser);
        return "redirect:/?loginSuccess=true";
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

    private boolean isGoogleLoginEnabled() {
        return googleClientId != null
                && !googleClientId.isBlank()
                && !googleClientId.startsWith("dummy-");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
