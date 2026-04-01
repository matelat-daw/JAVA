package com.futureprograms.MyIkea.Services;

import com.futureprograms.MyIkea.Interfaces.OrderInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.futureprograms.MyIkea.Models.Order;
import com.futureprograms.MyIkea.Models.Product;
import com.futureprograms.MyIkea.Models.Auth.User;
import com.futureprograms.MyIkea.Repositories.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class OrderService implements OrderInterface {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Integer id) {
        if (id == null) return Optional.empty();
        return orderRepository.findById(id);
    }

    @Override
    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order getCart(User user) {
        return orderRepository.findByCompletedFalseAndUser(user)
                .orElseGet(() -> createCart(user));
    }

    @Transactional
    public void addProductToCart(User user, Product product) {
        Order cart = getCart(user);
        cart.getProducts().add(product);
        double total = cart.getProducts().stream()
                .mapToDouble(p -> Objects.requireNonNullElse(p.getProductPrice(), 0.0f))
                .sum();
        cart.setTotalPrice(total);
        orderRepository.save(cart);
    }

    @Transactional
    public void removeProductFromCart(User user, Product product) {
        Order cart = getCart(user);
        if (cart.getProducts().remove(product)) {
            double total = cart.getProducts().stream()
                    .mapToDouble(p -> Objects.requireNonNullElse(p.getProductPrice(), 0.0f))
                    .sum();
            cart.setTotalPrice(total);
            orderRepository.save(cart);
        }
    }

    @Transactional
    public void completeOrder(User user) {
        orderRepository.findByCompletedFalseAndUser(user).ifPresent(order -> {
            order.setCompleted(true);
            order.setOrderDate(LocalDateTime.now());
            orderRepository.save(order);
        });
    }

    private Order createCart(User user) {
        Order newCart = new Order();
        newCart.setUser(user);
        newCart.setCompleted(false);
        newCart.setTotalPrice(0.0);
        newCart.setOrderDate(LocalDateTime.now());
        return orderRepository.save(newCart);
    }

    @Override
    @Transactional
    public List<Order> getCompletedOrders(User user) {
        List<Order> orders = orderRepository.findByCompletedTrueAndUser(user);
        boolean updated = false;

        for (Order order : orders) {
            if (order.getOrderDate() == null) {
                order.setOrderDate(LocalDateTime.now());
                updated = true;
            }
        }

        if (updated) {
            orderRepository.saveAll(orders);
        }

        return orders;
    }
}
