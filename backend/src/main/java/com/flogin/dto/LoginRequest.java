// src/main/java/com/flogin/dto/LoginRequest.java
package com.flogin.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Username không được để trống") String username,
        @NotBlank(message = "Password không được để trống") String password) {
}