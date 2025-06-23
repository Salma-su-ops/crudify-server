package com.example.crudify_server.service;

import com.example.crudify_server.dto.AuthRequest;
import com.example.crudify_server.dto.AuthResponse;
import com.example.crudify_server.dto.RegisterRequest;
import com.example.crudify_server.entity.User;
import com.example.crudify_server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRegisterRequest;
    private AuthRequest validAuthRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest("testuser", "test@example.com", "password123");
        validAuthRequest = new AuthRequest("testuser", "password123");
        
        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setId(1L);
    }

    @Test
    void register_WithValidRequest_ShouldCreateUserAndReturnAuthResponse() {
        when(userRepository.existsByUsername(validRegisterRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse result = userService.register(validRegisterRequest);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void register_WithExistingUsername_ShouldThrowRuntimeException() {
        when(userRepository.existsByUsername(validRegisterRequest.getUsername())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.register(validRegisterRequest));
        
        assertEquals("Username is already taken!", exception.getMessage());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowRuntimeException() {
        when(userRepository.existsByUsername(validRegisterRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.register(validRegisterRequest));
        
        assertEquals("Email is already in use!", exception.getMessage());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnAuthResponse() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByUsername(validAuthRequest.getUsername()))
            .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

        AuthResponse result = userService.authenticate(validAuthRequest);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("testuser", result.getUsername());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void authenticate_WithInvalidCredentials_ShouldThrowBadCredentialsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, 
            () -> userService.authenticate(validAuthRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void authenticate_WithNonExistentUser_ShouldThrowRuntimeException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByUsername(validAuthRequest.getUsername()))
            .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.authenticate(validAuthRequest));
        
        assertEquals("User not found", exception.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void loadUserByUsername_WithExistingUser_ShouldReturnUserDetails() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        var userDetails = userService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_WithNonExistentUser_ShouldThrowUsernameNotFoundException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, 
            () -> userService.loadUserByUsername("nonexistent"));
        
        assertEquals("User not found: nonexistent", exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
    }
} 