package com.flogin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flogin.dto.LoginRequest;
import com.flogin.dto.LoginResponse;
import com.flogin.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController Mock Tests")
class AuthControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private LoginRequest validLoginRequest;
    private LoginResponse successResponse;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest("testuser", "Test123");
        successResponse = new LoginResponse("Đăng nhập thành công", "dummy-token-for-testuser");
    }

    @Test
    @DisplayName("TC1: Login thành công (Happy Path)")
    void testLoginSuccess() throws Exception {
        when(authService.authenticate(any(LoginRequest.class)))
                .thenReturn(successResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.token").value("dummy-token-for-testuser"));

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("TC2: Login thất bại - Service ném lỗi (Sai pass/user)")
    void testLoginFailure_WrongCredentials() throws Exception {
        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Sai tên tài khoản hoặc mật khẩu!"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sai tên tài khoản hoặc mật khẩu!"))
                .andExpect(jsonPath("$.token").doesNotExist());

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("TC3: Login thất bại - Service ném lỗi (Tài khoản khóa)")
    void testLoginFailure_AccountLocked() throws Exception {
        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Tài khoản của bạn đã bị khóa!"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Tài khoản của bạn đã bị khóa!"))
                .andExpect(jsonPath("$.token").doesNotExist());

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("TC4: Validation - Username rỗng")
    void testValidation_EmptyUsername() throws Exception {
        LoginRequest request = new LoginRequest("", "Test123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).authenticate(any());
    }

    @Test
    @DisplayName("TC5: Validation - Username null")
    void testValidation_NullUsername() throws Exception {
        LoginRequest request = new LoginRequest(null, "Test123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).authenticate(any());
    }

    @Test
    @DisplayName("TC6: Validation - Password rỗng")
    void testValidation_EmptyPassword() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).authenticate(any());
    }

    @Test
    @DisplayName("TC7: Validation - Password null")
    void testValidation_NullPassword() throws Exception {
        LoginRequest request = new LoginRequest("testuser", null);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).authenticate(any());
    }

    @Test
    @DisplayName("TC8: Validation - Cả hai đều null")
    void testValidation_AllNull() throws Exception {
        LoginRequest request = new LoginRequest(null, null);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).authenticate(any());
    }

    @Test
    @DisplayName("TC9: Validation - Username quá ngắn (dưới 3)")
    void testValidation_UsernameTooShort() throws Exception {
        LoginRequest request = new LoginRequest("ab", "Test123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("TC10: Validation - Password quá ngắn (dưới 6)")
    void testValidation_PasswordTooShort() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "12345");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("TC11: Validation - Username quá dài (trên 50)")
    void testValidation_UsernameTooLong() throws Exception {
        String longUsername = "a".repeat(51);
        LoginRequest request = new LoginRequest(longUsername, "Test123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("TC12: Validation - Password quá dài (trên 255)")
    void testValidation_PasswordTooLong() throws Exception {
        String longPassword = "a".repeat(256);
        LoginRequest request = new LoginRequest("testuser", longPassword);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("TC13: Validation - Username có ký tự không hợp lệ")
    void testValidation_UsernameInvalidChars() throws Exception {
        LoginRequest request = new LoginRequest("test!user", "Test123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("TC14: Request Body - JSON không hợp lệ (Malformed)")
    void testBadRequest_MalformedJson() throws Exception {
        String malformedJson = "{\"username\":\"testuser\", \"password\":\"Test123\"";
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).authenticate(any());
    }

    @Test
    @DisplayName("TC15: Request Body - JSON rỗng {}")
    void testBadRequest_EmptyJson() throws Exception {
        String emptyJson = "{}";
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).authenticate(any());
    }

    @Test
    @DisplayName("TC16: Request Body - Trống (No body)")
    void testBadRequest_NoBody() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).authenticate(any());
    }

    @Test
    @DisplayName("TC17: Content-Type không hỗ trợ")
    void testUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_XML)
                .content("<login><user>test</user></login>"))
                .andExpect(status().isUnsupportedMediaType());

        verify(authService, never()).authenticate(any());
    }
}