package com.futureprograms.MyIkea.Api.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 512, message = "El nombre es demasiado largo")
        String name,

        @NotNull(message = "El precio es obligatorio")
        @Min(value = 0, message = "El precio debe ser mayor o igual a 0")
        Float price,

        String picture,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock debe ser mayor o igual a 0")
        Integer stock,

        @NotNull(message = "El municipio es obligatorio")
        Integer municipalityId
) {
}
