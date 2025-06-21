package com.example.crudify_server.controller;

import com.example.crudify_server.dto.ProductRequest;
import com.example.crudify_server.dto.ProductResponse;
import com.example.crudify_server.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductResponse testProductResponse;
    private ProductRequest testProductRequest;

    @BeforeEach
    void setUp() {
        testProductResponse = new ProductResponse(1L, "Test Product", "Test Description", 
                new BigDecimal("99.99"), 10, LocalDateTime.now(), LocalDateTime.now());
        
        testProductRequest = new ProductRequest("Test Product", "Test Description", 
                new BigDecimal("99.99"), 10);
    }

    @Test
    @WithMockUser
    void getAllProducts_ShouldReturnProductList() throws Exception {
        List<ProductResponse> products = Arrays.asList(testProductResponse);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].price").value(99.99));
    }

    @Test
    @WithMockUser
    void getProductById_WhenExists_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProductResponse));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    @WithMockUser
    void getProductById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(productService.getProductById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createProduct_WithValidData_ShouldReturnCreatedProduct() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(testProductResponse);

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUser
    void createProduct_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        ProductRequest invalidRequest = new ProductRequest("", "", new BigDecimal("-1"), -1);

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateProduct_WhenExists_ShouldReturnUpdatedProduct() throws Exception {
        when(productService.updateProduct(eq(1L), any(ProductRequest.class)))
                .thenReturn(Optional.of(testProductResponse));

        mockMvc.perform(put("/api/products/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUser
    void updateProduct_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(productService.updateProduct(eq(1L), any(ProductRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/products/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteProduct_WhenExists_ShouldReturnNoContent() throws Exception {
        when(productService.deleteProduct(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/products/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteProduct_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(productService.deleteProduct(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/products/1")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void searchProducts_ShouldReturnMatchingProducts() throws Exception {
        List<ProductResponse> products = Arrays.asList(testProductResponse);
        when(productService.searchProductsByName("Test")).thenReturn(products);

        mockMvc.perform(get("/api/products/search")
                .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }
} 