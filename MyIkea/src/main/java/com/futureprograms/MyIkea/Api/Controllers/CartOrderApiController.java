package com.futureprograms.MyIkea.Api.Controllers;

import com.futureprograms.MyIkea.Api.Dto.OrderDto;
import com.futureprograms.MyIkea.Api.Mapper.ApiMapper;
import com.futureprograms.MyIkea.Models.Auth.User;
import com.futureprograms.MyIkea.Services.OrderService;
import com.futureprograms.MyIkea.Services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CartOrderApiController {

    private final ProductService productService;
    private final OrderService orderService;

    public CartOrderApiController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping("/cart")
    public ResponseEntity<OrderDto> getCart(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(ApiMapper.toOrderDto(orderService.getCart(user)));
    }

    @PostMapping("/cart/items/{productId}")
    public ResponseEntity<?> addItem(@PathVariable Integer productId, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        var product = productService.getProductById(productId);
        if (product.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Producto no encontrado"));
        }

        orderService.addProductToCart(user, product.get());
        return ResponseEntity.ok(ApiMapper.toOrderDto(orderService.getCart(user)));
    }

    @DeleteMapping("/cart/items/{productId}")
    public ResponseEntity<?> removeItem(@PathVariable Integer productId, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        var product = productService.getProductById(productId);
        if (product.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Producto no encontrado"));
        }

        orderService.removeProductFromCart(user, product.get());
        return ResponseEntity.ok(ApiMapper.toOrderDto(orderService.getCart(user)));
    }

    @PostMapping("/orders/complete")
    public ResponseEntity<?> completeOrder(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        orderService.completeOrder(user);
        return ResponseEntity.ok(Map.of("message", "Pedido completado"));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDto>> getOrders(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        List<OrderDto> orders = orderService.getCompletedOrders(user).stream()
                .map(ApiMapper::toOrderDto)
                .toList();

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Integer id, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        return orderService.getOrderById(id)
                .filter(order -> order.getUser() != null && order.getUser().getId().equals(user.getId()))
                .map(ApiMapper::toOrderDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
