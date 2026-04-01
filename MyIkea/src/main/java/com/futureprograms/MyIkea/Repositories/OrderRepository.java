package com.futureprograms.MyIkea.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.futureprograms.MyIkea.Models.Order;
import com.futureprograms.MyIkea.Models.Auth.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByCompletedFalseAndUser(User user);
    List<Order> findByCompletedTrueAndUser(User user);
}
