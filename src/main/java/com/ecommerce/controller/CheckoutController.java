package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.CartService;
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
    private final CartService cartService;

    public CheckoutController(CheckoutService checkoutService,
                              OrderService orderService,
                              CartService cartService) {
        this.checkoutService = checkoutService;
        this.orderService = orderService;
        this.cartService = cartService;
    }

    // 1. Create Razorpay order dynamically based on the items in the user's cart
    @PostMapping("/create-order")
    public Map<String, Object> createOrder() throws Exception {

        // Calculate dynamic total price of cart items by multiplying price and quantity
        double amount = cartService.getCartItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        if (amount <= 0) {
            throw new IllegalArgumentException("Cart cannot be empty for checkout!");
        }

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

        orderService.createOrder(user);

        return "OK";
    }
}