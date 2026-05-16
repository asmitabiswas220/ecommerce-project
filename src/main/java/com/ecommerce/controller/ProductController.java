package com.ecommerce.controller;

import com.ecommerce.model.User;

import com.ecommerce.model.Product;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CartService cartService;
    private final com.ecommerce.service.FileStorageService fileStorageService;
    private final OrderService orderService;

    public ProductController(ProductService productService,
            CartService cartService,
            com.ecommerce.service.FileStorageService fileStorageService, OrderService orderService) {

        this.productService = productService;
        this.cartService = cartService;
        this.fileStorageService = fileStorageService;
        this.orderService = orderService;
    }

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute(
                "products",
                productService.getAllProducts());

        return "index";
    }

    @GetMapping("/add-to-cart/{id}")
    public String addToCart(@PathVariable Long id) {

        Product product = productService.getProductById(id);

        if (product != null) {
            cartService.addToCart(product);
        }

        return "redirect:/";
    }

    @GetMapping("/cart")
    public String cart(Model model) {

        model.addAttribute(
                "cartItems",
                cartService.getCartItems());

        return "cart";
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {

        model.addAttribute(
                "product",
                new Product());

        return "admin";
    }

    @PostMapping("/add-product")
    public String addProduct(@ModelAttribute Product product,
            @RequestParam("image") org.springframework.web.multipart.MultipartFile image) {

        if (!image.isEmpty()) {
            String imageUrl = fileStorageService.saveFile(image);
            product.setImageUrl(imageUrl);
        }

        productService.addProduct(product);

        return "redirect:/";
    }

    @GetMapping("/delete-product/{id}")
    public String deleteProduct(@PathVariable Long id) {

        productService.deleteProduct(id);

        return "redirect:/";
    }

    @GetMapping("/remove-from-cart/{id}")
    public String removeFromCart(@PathVariable Long id) {

        cartService.removeFromCart(id);

        return "redirect:/cart";
    }

    @PostMapping("/order/confirm")
    public String confirmPayment(@RequestBody String paymentData, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        orderService.createOrder(user); // you already have this

        return "Payment Success";
    }
}