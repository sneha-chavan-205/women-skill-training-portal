package com.wstp.portal.controller;

import com.wstp.portal.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegistrationRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password,
        Role role) {
}
