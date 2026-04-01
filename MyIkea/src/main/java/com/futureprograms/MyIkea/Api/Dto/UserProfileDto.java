package com.futureprograms.MyIkea.Api.Dto;

import java.util.List;

public record UserProfileDto(
        Integer id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String profilePicture,
        String gender,
        List<String> roles
) {
}
