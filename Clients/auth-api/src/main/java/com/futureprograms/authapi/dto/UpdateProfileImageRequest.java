package com.futureprograms.authapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar la foto de perfil del usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileImageRequest {

    @NotBlank(message = "La URL de la imagen es requerida")
    private String profileImageUrl;

    /**
     * Descripción de la imagen (opcional)
     */
    private String description;
}
