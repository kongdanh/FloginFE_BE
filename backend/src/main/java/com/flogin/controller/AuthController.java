package com.flogin.controller;

import com.flogin.dto.LoginRequest;
import com.flogin.dto.LoginResponse;
import com.flogin.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
// Cho phép React (localhost:5173 hoặc 3000) gọi API này
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:8080" })
public class AuthController {

    @Autowired
    private AuthService authService;

    // src/main/java/com/flogin/controller/AuthController.java
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            BindingResult bindingResult) { // BẮT BUỘC có tham số này

        // KIỂM TRA LỖI VALIDATION TRƯỚC
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .findFirst()
                    .orElse("Sai tên tài khoản hoặc mật khẩu!");
            return ResponseEntity.status(401).body(new LoginResponse(message, null));
        }

        try {
            LoginResponse response = authService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(new LoginResponse(e.getMessage(), null));
        }
    }
}