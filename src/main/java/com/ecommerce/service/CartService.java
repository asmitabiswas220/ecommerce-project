package com.ecommerce.service;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;

@Service
@SessionScope
public class CartService {

    private final List<CartItem> cartItems = new ArrayList<>();

    public void addToCart(Product product) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        cartItems.add(new CartItem(product, 1));
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public int getTotalQuantity() {
        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public double getSubtotal() {
        return cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    public void removeFromCart(Long id) {
        cartItems.removeIf(item -> item.getProduct().getId().equals(id));
    }

    public void incrementQuantity(Long id) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(id)) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
    }

    public void decrementQuantity(Long id) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(id)) {
                item.setQuantity(item.getQuantity() - 1);
                break;
            }
        }
        cartItems.removeIf(item -> item.getQuantity() <= 0);
    }
}
