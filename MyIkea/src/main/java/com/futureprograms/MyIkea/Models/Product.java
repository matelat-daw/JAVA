package com.futureprograms.MyIkea.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Entity
@Table(name = "productoffer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Size(max = 512, message = "El nombre del producto es demasiado largo")
    @Column(nullable = false, length = 512, name = "product_name")
    private String productName;

    @NotNull(message = "El precio del producto es obligatorio")
    @Min(value = 0, message = "El precio debe ser mayor o igual a 0")
    @Column(name = "product_price")
    private Float productPrice;

    @Column(length = 512, name = "product_picture")
    private String productPicture;

    @NotNull(message = "El municipio es obligatorio")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_municipio", nullable = false)
    private Municipality municipality;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock debe ser mayor o igual a 0")
    @Column(nullable = false, name = "product_stock")
    private Integer productStock;

    @ManyToMany(mappedBy = "products")
    private List<Order> orders;

    @Transient
    private MultipartFile productPictureFile;
}