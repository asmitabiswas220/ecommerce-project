package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/auth/otp")
public class OtpController {

    private final UserService userService;
    private final Random random = new Random();

    public OtpController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/send")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestParam String phone, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // Validate basic 10-digit number format
        if (phone == null || !phone.matches("\\d{10}")) {
            response.put("status", "ERROR");
            response.put("message", "Invalid mobile number. Please enter a 10-digit number.");
            return ResponseEntity.badRequest().body(response);
        }

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", 100000 + random.nextInt(900000));

        // Store OTP and phone in session
        session.setAttribute("otp", otpCode);
        session.setAttribute("otpPhone", phone);

        // Mock SMS Gateway Logging in Console
        System.out.println("\n==================================================");
        System.out.println("🔑 [MOCK SMS GATEWAY] SENDING OTP VERIFICATION CODE");
        System.out.println("📱 Target Phone: +91 " + phone);
        System.out.println("📨 Verification Code: " + otpCode);
        System.out.println("==================================================\n");

        response.put("status", "SUCCESS");
        response.put("message", "OTP sent successfully via Mock SMS Gateway!");
        response.put("otp", otpCode); // Included for transparent client-side mock notifications
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestParam String phone,
                                                         @RequestParam String otp,
                                                         HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        String sessionOtp = (String) session.getAttribute("otp");
        String sessionPhone = (String) session.getAttribute("otpPhone");

        if (sessionOtp == null || sessionPhone == null || !sessionPhone.equals(phone)) {
            response.put("status", "ERROR");
            response.put("message", "Session expired or invalid mobile number. Please request a new OTP.");
            return ResponseEntity.badRequest().body(response);
        }

        if (!sessionOtp.equals(otp)) {
            response.put("status", "ERROR");
            response.put("message", "Incorrect verification code. Please try again.");
            return ResponseEntity.badRequest().body(response);
        }

        // Clear OTP from session upon successful verification
        session.removeAttribute("otp");
        session.removeAttribute("otpPhone");

        // Find existing user or auto-register a new profile by phone
        User user = userService.findByPhoneNumber(phone)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setName("Guest User " + phone.substring(6));
                    newUser.setUsername(phone);
                    newUser.setPassword("OTP_USER");
                    newUser.setPhoneNumber(phone);
                    return userService.register(newUser);
                });

        // Log the user into the HTTP session
        session.setAttribute("loggedInUser", user);

        response.put("status", "SUCCESS");
        response.put("message", "Login successful!");
        return ResponseEntity.ok(response);
    }
}
