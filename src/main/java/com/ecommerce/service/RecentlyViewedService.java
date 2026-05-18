package com.ecommerce.service;

import com.ecommerce.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;

@Service
@SessionScope
public class RecentlyViewedService {

    private static final int MAX_ITEMS = 5;
    private final List<Product> recentItems = new ArrayList<>();

    public void record(Product product) {
        // Remove if already present (re-insert at front)
        recentItems.removeIf(p -> p.getId().equals(product.getId()));
        recentItems.add(0, product);
        if (recentItems.size() > MAX_ITEMS) {
            recentItems.remove(recentItems.size() - 1);
        }
    }

    public List<Product> getRecentItems() {
        return recentItems;
    }

    public boolean hasItems() {
        return !recentItems.isEmpty();
    }
}
