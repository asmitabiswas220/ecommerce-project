package com.ecommerce.service;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    public Order createOrder(User user) {
        List<Product> cartItems = cartService.getCartItems();

        if (cartItems.isEmpty()) {
            return null;
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        double total = 0;
        for (Product p : cartItems) {
            OrderItem item = new OrderItem();
            item.setProduct(p);
            item.setPrice(p.getPrice());
            item.setQuantity(1); // Default to 1 for this simple cart
            order.addItem(item);
            total += p.getPrice();
        }

        order.setTotalAmount(total);

        orderRepository.save(order);

        // Clear cart after successful order
        cartItems.clear();

        return order;
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
}
