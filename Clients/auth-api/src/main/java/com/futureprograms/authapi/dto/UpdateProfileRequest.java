package com.futureprograms.authapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar información del perfil del usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String name;

    @NotBlank(message = "El primer apellido es requerido")
    @Size(min = 2, max = 50, message = "El primer apellido debe tener entre 2 y 50 caracteres")
    private String surname1;

    @Size(min = 0, max = 50, message = "El segundo apellido debe tener máximo 50 caracteres")
    private String surname2;

    @NotBlank(message = "El teléfono es requerido")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono debe ser válido (formato: +34612345678 o 612345678)")
    private String phone;
}
