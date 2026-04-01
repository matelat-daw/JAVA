package com.futureprograms.MyIkea.Controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.futureprograms.MyIkea.Models.Order;
import com.futureprograms.MyIkea.Models.Auth.User;
import com.futureprograms.MyIkea.Services.OrderService;
import com.futureprograms.MyIkea.Services.ProductService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Controller
@RequestMapping("/")
@Slf4j
public class OrdersController {
    private final ProductService productService;
    private final OrderService orderService;

    public OrdersController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping("/cart")
    public String cart(Model model, @AuthenticationPrincipal User user) {
        Order cart = orderService.getCart(user);
        model.addAttribute("cart", cart);
        return "cart/cart";
    }

    @GetMapping("/cart/add/{id}")
    public String addCart(@PathVariable Integer id, @AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/login";
        
        productService.getProductById(id).ifPresentOrElse(
                product -> {
                    orderService.addProductToCart(user, product);
                    log.info("Product {} added to cart for user {}", id, user.getEmail());
                },
                () -> log.warn("Attempt to add non-existent product {} to cart", id)
        );
        return "redirect:/cart";
    }

    @GetMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Integer id, @AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/login";

        productService.getProductById(id).ifPresentOrElse(
                product -> {
                    orderService.removeProductFromCart(user, product);
                    log.info("Product {} removed from cart for user {}", id, user.getEmail());
                },
                () -> log.warn("Attempt to remove non-existent product {} from cart", id)
        );
        return "redirect:/cart";
    }

    @GetMapping("/orders")
    public String orders(Model model, @AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/login";

        List<Order> orders = orderService.getCompletedOrders(user);
        model.addAttribute("orders", orders);
        return "orders/orders";
    }

    @GetMapping("/orders/complete")
    public String completeOrder(@AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/login";

        orderService.completeOrder(user);
        log.info("Order completed for user {}", user.getEmail());
        return "redirect:/orders";
    }

    @GetMapping("/orders/details/{id}")
    public String orderDetails(@PathVariable Integer id, Model model) {
        orderService.getOrderById(id).ifPresentOrElse(
                order -> model.addAttribute("order", order),
                () -> model.addAttribute("error", "Order not found")
        );
        return "orders/details";
    }

    @GetMapping("/orders/details/{id}/pay")
    public String payOrderSimulated(@PathVariable Integer id) {
        // Simulated payment
        return "redirect:/orders/details/" + id + "?pago=ok";
    }
}
