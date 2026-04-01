package com.futureprograms.authapi.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Nick is required")
    @Size(min = 3, max = 20, message = "Nick must be between 3 and 20 characters")
    private String nick;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "First surname is required")
    @Size(min = 2, max = 50, message = "Surname1 must be between 2 and 50 characters")
    private String surname1;

    @Size(max = 50, message = "Surname2 must be less than 50 characters")
    private String surname2;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number is invalid")
    private String phone;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(male|female|other)$", message = "Gender must be one of: male, female, other")
    private String gender;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!\\-._*]).*$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String password;

    private String profileImg;

    @PastOrPresent(message = "Birthday cannot be in the future")
    private LocalDate bday;
}
