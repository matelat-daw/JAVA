package com.futureprograms.MyIkea.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.futureprograms.MyIkea.Models.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
}
