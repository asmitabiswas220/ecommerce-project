package com.ecommerce.service;

import com.ecommerce.model.CartItem;
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
        List<CartItem> cartItems = cartService.getCartItems();

        if (cartItems.isEmpty()) {
            return null;
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        double total = 0;
        for (CartItem item : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(item.getProduct());
            orderItem.setPrice(item.getProduct().getPrice());
            orderItem.setQuantity(item.getQuantity());
            order.addItem(orderItem);
            total += item.getProduct().getPrice() * item.getQuantity();
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
