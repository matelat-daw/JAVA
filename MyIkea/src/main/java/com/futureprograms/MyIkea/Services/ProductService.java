package com.futureprograms.MyIkea.Services;

import com.futureprograms.MyIkea.Interfaces.ProductInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.futureprograms.MyIkea.Models.Product;
import com.futureprograms.MyIkea.Repositories.ProductRepository;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductService implements ProductInterface {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductById(Integer id) {
        if (id == null) return Optional.empty();
        return productRepository.findById(id);
    }

    @Override
    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        if (id != null) {
            productRepository.deleteById(id);
        }
    }
}
