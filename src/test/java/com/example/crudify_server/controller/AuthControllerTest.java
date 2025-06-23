package com.example.crudify_server.controller;

import com.example.crudify_server.dto.AuthRequest;
import com.example.crudify_server.dto.AuthResponse;
import com.example.crudify_server.dto.RegisterRequest;
import com.example.crudify_server.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest validRegisterRequest;
    private AuthRequest validAuthRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest("testuser", "test@example.com", "password123");
        validAuthRequest = new AuthRequest("testuser", "password123");
        authResponse = new AuthResponse("jwt-token", "testuser", "test@example.com", "USER");
    }

    @Test
    void registerUser_WithValidRequest_ShouldReturnAuthResponse() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void registerUser_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", "", "");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void registerUser_WithDuplicateUsername_ShouldReturnBadRequest() throws Exception {
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Username is already taken!"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username is already taken!"));

        verify(userService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void registerUser_WithDuplicateEmail_ShouldReturnBadRequest() throws Exception {
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Email is already in use!"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpected(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is already in use!"));

        verify(userService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void loginUser_WithValidCredentials_ShouldReturnAuthResponse() throws Exception {
        when(userService.authenticate(any(AuthRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService, times(1)).authenticate(any(AuthRequest.class));
    }

    @Test
    void loginUser_WithInvalidCredentials_ShouldReturnBadRequest() throws Exception {
        when(userService.authenticate(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        verify(userService, times(1)).authenticate(any(AuthRequest.class));
    }

    @Test
    void loginUser_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        AuthRequest invalidRequest = new AuthRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).authenticate(any(AuthRequest.class));
    }

    @Test
    void registerUser_WithMissingContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isUnsupportedMediaType());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void loginUser_WithMissingContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isUnsupportedMediaType());

        verify(userService, never()).authenticate(any(AuthRequest.class));
    }
} 