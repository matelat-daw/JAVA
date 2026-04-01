package com.futureprograms.MyIkea.Api.Dto;

public record ProfileUpdateRequest(
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        String gender
) {
}
