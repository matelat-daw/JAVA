package com.futureprograms.MyIkea.Interfaces;

import com.futureprograms.MyIkea.Models.Product;
import java.util.List;
import java.util.Optional;

/**
 * Interface contract for Product operations
 */
public interface ProductInterface {
    List<Product> getAllProducts();
    Optional<Product> getProductById(Integer id);
    Product saveProduct(Product product);
    void deleteProduct(Integer id);
}
