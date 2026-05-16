package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.CheckoutService;
import com.ecommerce.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final OrderService orderService;

    public CheckoutController(CheckoutService checkoutService,
            OrderService orderService) {
        this.checkoutService = checkoutService;
        this.orderService = orderService;
    }

    // 1. Create Razorpay order
    @PostMapping("/create-order")
    public Map<String, Object> createOrder() throws Exception {

        double amount = 1000; // later we will calculate cart total dynamically

        var razorOrder = checkoutService.createRazorpayOrder(amount);

        Map<String, Object> response = new HashMap<>();
        response.put("id", razorOrder.get("id"));
        response.put("amount", razorOrder.get("amount"));

        return response;
    }

    // 2. After payment success
    @PostMapping("/confirm")
    public String confirmPayment(@RequestBody Map<String, String> data,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        orderService.createOrder(user); // your existing logic

        return "OK";
    }
}