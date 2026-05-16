package com.ecommerce.service;

import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {

    private final RazorpayClient razorpayClient;

    public CheckoutService(RazorpayClient razorpayClient) {
        this.razorpayClient = razorpayClient;
    }

    public com.razorpay.Order createRazorpayOrder(double amount) throws Exception {

        JSONObject options = new JSONObject();
        options.put("amount", amount * 100); // paise
        options.put("currency", "INR");
        options.put("receipt", "order_rcptid_" + System.currentTimeMillis());

        return razorpayClient.orders.create(options);
    }
}