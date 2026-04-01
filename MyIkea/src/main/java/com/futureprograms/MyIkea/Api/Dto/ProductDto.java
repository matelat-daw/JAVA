package com.futureprograms.MyIkea.Api.Dto;

public record ProductDto(
        Integer id,
        String name,
        Float price,
        String picture,
        Integer stock,
        Integer municipalityId
) {
}
