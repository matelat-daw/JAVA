package com.futureprograms.MyIkea.Interfaces;

import com.futureprograms.MyIkea.Models.Order;
import com.futureprograms.MyIkea.Models.Auth.User;
import java.util.List;
import java.util.Optional;

/**
 * Interface contract for Order operations
 */
public interface OrderInterface {
    List<Order> getAllOrders();
    Optional<Order> getOrderById(Integer id);
    Order saveOrder(Order order);
    Order getCart(User user);
    List<Order> getCompletedOrders(User user);
}
