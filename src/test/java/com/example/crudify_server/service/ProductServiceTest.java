package com.example.crudify_server.service;

import com.example.crudify_server.dto.ProductRequest;
import com.example.crudify_server.dto.ProductResponse;
import com.example.crudify_server.entity.Product;
import com.example.crudify_server.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductRequest testRequest;

    @BeforeEach
    void setUp() {
        testProduct = new Product("Test Product", "Test Description", new BigDecimal("99.99"), 10);
        testProduct.setId(1L);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());

        testRequest = new ProductRequest("Test Product", "Test Description", new BigDecimal("99.99"), 10);
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        List<ProductResponse> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productRepository).findAll();
    }

    @Test
    void getProductById_WhenExists_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Optional<ProductResponse> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().getName());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_WhenNotExists_ShouldReturnEmpty() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<ProductResponse> result = productService.getProductById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse result = productService.createProduct(testRequest);

        assertEquals("Test Product", result.getName());
        assertEquals(new BigDecimal("99.99"), result.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenExists_ShouldReturnUpdatedProduct() {
        Product updatedProduct = new Product("Updated Product", "Updated Description", new BigDecimal("199.99"), 20);
        updatedProduct.setId(1L);
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductRequest updateRequest = new ProductRequest("Updated Product", "Updated Description", new BigDecimal("199.99"), 20);
        Optional<ProductResponse> result = productService.updateProduct(1L, updateRequest);

        assertTrue(result.isPresent());
        assertEquals("Updated Product", result.get().getName());
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenNotExists_ShouldReturnEmpty() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<ProductResponse> result = productService.updateProduct(1L, testRequest);

        assertFalse(result.isPresent());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_WhenExists_ShouldReturnTrue() {
        when(productRepository.existsById(1L)).thenReturn(true);

        boolean result = productService.deleteProduct(1L);

        assertTrue(result);
        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_WhenNotExists_ShouldReturnFalse() {
        when(productRepository.existsById(anyLong())).thenReturn(false);

        boolean result = productService.deleteProduct(1L);

        assertFalse(result);
        verify(productRepository).existsById(1L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void searchProductsByName_ShouldReturnMatchingProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByNameContainingIgnoreCase("Test")).thenReturn(products);

        List<ProductResponse> result = productService.searchProductsByName("Test");

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productRepository).findByNameContainingIgnoreCase("Test");
    }
} 