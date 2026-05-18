package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.model.Product;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.WishlistService;
import com.ecommerce.service.RecentlyViewedService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class ProductController {

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${app.admin.emails:asmitabiswas220@gmail.com,admin@store.com}")
    private java.util.List<String> adminEmails;

    private final ProductService productService;
    private final CartService cartService;
    private final com.ecommerce.service.FileStorageService fileStorageService;
    private final OrderService orderService;
    private final WishlistService wishlistService;
    private final RecentlyViewedService recentlyViewedService;

    public ProductController(ProductService productService,
            CartService cartService,
            com.ecommerce.service.FileStorageService fileStorageService,
            OrderService orderService,
            WishlistService wishlistService,
            RecentlyViewedService recentlyViewedService) {

        this.productService = productService;
        this.cartService = cartService;
        this.fileStorageService = fileStorageService;
        this.orderService = orderService;
        this.wishlistService = wishlistService;
        this.recentlyViewedService = recentlyViewedService;
    }

    @ModelAttribute("cartCount")
    public int getCartCount() {
        if (cartService == null || cartService.getCartItems() == null) {
            return 0;
        }
        return cartService.getCartItems().size();
    }

    @ModelAttribute("wishlistCount")
    public int getWishlistCount() {
        return wishlistService.getWishlistCount();
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(HttpSession session) {
        return checkAdmin(session);
    }

    private boolean checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return false;
        }
        return adminEmails.contains(user.getUsername());
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", productService.getCategories());
        java.util.Set<Long> wishlistIds = new java.util.HashSet<>();
        wishlistService.getWishlistItems().forEach(p -> wishlistIds.add(p.getId()));
        model.addAttribute("wishlistIds", wishlistIds);
        model.addAttribute("recentlyViewed", recentlyViewedService.getRecentItems());
        model.addAttribute("hasRecentlyViewed", recentlyViewedService.hasItems());
        return "index";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) return "redirect:/";
        // Track recently viewed
        recentlyViewedService.record(product);
        model.addAttribute("product", product);
        model.addAttribute("inWishlist", wishlistService.isInWishlist(id));
        // Related products (same category, excluding current)
        String cat = product.getCategory() != null ? product.getCategory() : "";
        model.addAttribute("relatedProducts", productService.getRelatedProducts(cat, id));
        return "product-detail";
    }

    /** AJAX endpoint — returns JSON so the cart badge can update without a page reload */
    @GetMapping("/cart/ajax-add/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> ajaxAddToCart(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product != null) cartService.addToCart(product);
        int newCount = cartService.getCartItems().size();
        return ResponseEntity.ok(Map.of("success", true, "cartCount", newCount,
                "productName", product != null ? product.getName() : ""));
    }

    @GetMapping("/wishlist")
    public String wishlist(Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";
        model.addAttribute("wishlistItems", wishlistService.getWishlistItems());
        return "wishlist";
    }

    @GetMapping("/wishlist/add/{id}")
    public String addToWishlist(@PathVariable Long id,
                                @RequestParam(defaultValue="/") String redirect) {
        Product product = productService.getProductById(id);
        if (product != null) wishlistService.addToWishlist(product);
        return "redirect:" + redirect;
    }

    @GetMapping("/wishlist/remove/{id}")
    public String removeFromWishlist(@PathVariable Long id,
                                     @RequestParam(defaultValue="/wishlist") String redirect) {
        wishlistService.removeFromWishlist(id);
        return "redirect:" + redirect;
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
        model.addAttribute(
                "razorpayKeyId",
                razorpayKeyId);

        return "cart";
    }

    @GetMapping("/orders")
    public String orders(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("orders", orderService.getOrdersByUser(user));
        return "orders";
    }

    @GetMapping("/admin")
    public String adminPage(Model model, HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/";
        }
        model.addAttribute(
                "product",
                new Product());

        return "admin";
    }

    @PostMapping("/add-product")
    public String addProduct(@ModelAttribute Product product,
            @RequestParam("image") org.springframework.web.multipart.MultipartFile image,
            HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/";
        }
        if (!image.isEmpty()) {
            String imageUrl = fileStorageService.saveFile(image);
            product.setImageUrl(imageUrl);
        }

        productService.addProduct(product);

        return "redirect:/";
    }

    @GetMapping("/delete-product/{id}")
    public String deleteProduct(@PathVariable Long id, HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/";
        }
        productService.deleteProduct(id);

        return "redirect:/";
    }

    @GetMapping("/remove-from-cart/{id}")
    public String removeFromCart(@PathVariable Long id) {

        cartService.removeFromCart(id);

        return "redirect:/cart";
    }

    @GetMapping("/cart/increment/{id}")
    public String incrementCartItem(@PathVariable Long id) {
        cartService.incrementQuantity(id);
        return "redirect:/cart";
    }

    @GetMapping("/cart/decrement/{id}")
    public String decrementCartItem(@PathVariable Long id) {
        cartService.decrementQuantity(id);
        return "redirect:/cart";
    }

    @PostMapping("/order/confirm")
    public String confirmPayment(@RequestBody String paymentData, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        orderService.createOrder(user); // you already have this

        return "Payment Success";
    }
}
