package com.futureprograms.MyIkea.Api.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @NotBlank(message = "username is required") String username,
        @NotBlank(message = "email is required") @Email(message = "email is invalid") String email,
        @NotBlank(message = "password is required") @Size(min = 8, message = "password must have at least 8 characters") String password,
        @NotBlank(message = "confirmPassword is required") String confirmPassword,
        String gender
) {
}
