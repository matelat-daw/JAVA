package com.futureprograms.MyIkea.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.futureprograms.MyIkea.Models.Auth.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer orderId;

    @NotNull(message = "El precio total es obligatorio")
    @PositiveOrZero(message = "El precio total no puede ser negativo")
    @Column(name = "total_Price")
    private Double totalPrice;

    @Column(name = "fecha_pedido")
    private LocalDateTime orderDate;

    @Column
    private Boolean completed;

    @ManyToMany
    @JoinTable(
            name = "product_pedido",
            joinColumns = @JoinColumn(name = "id_pedido"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id", nullable = false)
    private User user;

    @PrePersist
    @PreUpdate
    private void ensureOrderDate() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }
}
