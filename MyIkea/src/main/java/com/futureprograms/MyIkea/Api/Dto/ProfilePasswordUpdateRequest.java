package com.futureprograms.MyIkea.Api.Dto;

public record ProfilePasswordUpdateRequest(
        String currentPassword,
        String newPassword,
        String confirmPassword
) {
}