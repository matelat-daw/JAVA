package com.futureprograms.MyIkea.Api.Dto;

public record AuthResponse(
        String message,
        UserProfileDto user
) {
}
