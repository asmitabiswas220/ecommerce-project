package com.ecommerce.service;

import com.ecommerce.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;

@Service
@SessionScope
public class WishlistService {

    private final List<Product> wishlistItems = new ArrayList<>();

    public void addToWishlist(Product product) {
        boolean exists = wishlistItems.stream()
                .anyMatch(p -> p.getId().equals(product.getId()));
        if (!exists) {
            wishlistItems.add(product);
        }
    }

    public void removeFromWishlist(Long productId) {
        wishlistItems.removeIf(p -> p.getId().equals(productId));
    }

    public boolean isInWishlist(Long productId) {
        return wishlistItems.stream().anyMatch(p -> p.getId().equals(productId));
    }

    public List<Product> getWishlistItems() {
        return wishlistItems;
    }

    public int getWishlistCount() {
        return wishlistItems.size();
    }
}
