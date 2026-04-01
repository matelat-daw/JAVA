package com.futureprograms.MyIkea.Api.Dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
        @NotBlank(message = "usernameOrEmail is required") String usernameOrEmail,
        @NotBlank(message = "password is required") String password
) {
}
