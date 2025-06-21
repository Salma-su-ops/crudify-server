package com.example.crudify_server.service;

import com.example.crudify_server.dto.ProductRequest;
import com.example.crudify_server.dto.ProductResponse;
import com.example.crudify_server.entity.Product;
import com.example.crudify_server.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public Optional<ProductResponse> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToResponse);
    }
    
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getQuantity()
        );
        
        Product savedProduct = productRepository.save(product);
        return convertToResponse(savedProduct);
    }
    
    public Optional<ProductResponse> updateProduct(Long id, ProductRequest request) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(request.getName());
                    product.setDescription(request.getDescription());
                    product.setPrice(request.getPrice());
                    product.setQuantity(request.getQuantity());
                    
                    Product updatedProduct = productRepository.save(product);
                    return convertToResponse(updatedProduct);
                });
    }
    
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<ProductResponse> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<ProductResponse> getProductsWithMinimumQuantity(Integer minQuantity) {
        return productRepository.findProductsWithMinimumQuantity(minQuantity)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private ProductResponse convertToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
} 