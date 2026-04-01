package com.futureprograms.MyIkea.Api.Dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Integer id,
        Double total,
        Boolean completed,
        LocalDateTime date,
        List<ProductDto> products
) {
}
