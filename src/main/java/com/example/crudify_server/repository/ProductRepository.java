package com.example.crudify_server.repository;

import com.example.crudify_server.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Product p WHERE p.quantity > :minQuantity")
    List<Product> findProductsWithMinimumQuantity(@Param("minQuantity") Integer minQuantity);
    
    List<Product> findByQuantityGreaterThan(Integer quantity);
} 