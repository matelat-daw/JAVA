package com.futureprograms.authapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de cambio de rol (solo para administradores)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequest {

    @NotNull(message = "El ID del usuario es requerido")
    @Positive(message = "El ID del usuario debe ser positivo")
    private Long userId;

    @NotBlank(message = "El nuevo rol es requerido")
    private String newRole;
}
