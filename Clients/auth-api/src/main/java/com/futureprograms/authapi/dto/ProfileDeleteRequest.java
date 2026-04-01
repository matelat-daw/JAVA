package com.futureprograms.authapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para eliminar la cuenta del usuario
 * Requiere confirmación con contraseña
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDeleteRequest {

    @NotBlank(message = "La contraseña es requerida para eliminar la cuenta")
    private String password;
}
