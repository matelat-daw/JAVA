package com.futureprograms.MyIkea.Api.Mapper;

import com.futureprograms.MyIkea.Api.Dto.MunicipalityDto;
import com.futureprograms.MyIkea.Api.Dto.OrderDto;
import com.futureprograms.MyIkea.Api.Dto.ProductDto;
import com.futureprograms.MyIkea.Api.Dto.ProvinceDto;
import com.futureprograms.MyIkea.Api.Dto.UserProfileDto;
import com.futureprograms.MyIkea.Models.Auth.User;
import com.futureprograms.MyIkea.Models.Municipality;
import com.futureprograms.MyIkea.Models.Order;
import com.futureprograms.MyIkea.Models.Product;
import com.futureprograms.MyIkea.Models.Province;

import java.util.List;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static ProductDto toProductDto(Product product) {
        if (product == null) {
            return null;
        }

        Integer municipalityId = product.getMunicipality() != null
                ? product.getMunicipality().getMunicipalityId()
                : null;

        return new ProductDto(
                product.getProductId(),
                product.getProductName(),
                product.getProductPrice(),
                product.getProductPicture(),
                product.getProductStock(),
                municipalityId
        );
    }

    public static OrderDto toOrderDto(Order order) {
        if (order == null) {
            return null;
        }

        List<ProductDto> productDtos = order.getProducts() == null
                ? List.of()
                : order.getProducts().stream().map(ApiMapper::toProductDto).toList();

        return new OrderDto(
                order.getOrderId(),
                order.getTotalPrice(),
                order.getCompleted(),
                order.getOrderDate(),
                productDtos
        );
    }

    public static ProvinceDto toProvinceDto(Province province) {
        if (province == null) {
            return null;
        }

        return new ProvinceDto(
                province.getProvinceId(),
                province.getName()
        );
    }

    public static MunicipalityDto toMunicipalityDto(Municipality municipality) {
        if (municipality == null) {
            return null;
        }

        Integer provinceId = municipality.getProvince() != null
                ? municipality.getProvince().getProvinceId()
                : null;

        return new MunicipalityDto(
                municipality.getMunicipalityId(),
                municipality.getName(),
                provinceId
        );
    }

    public static UserProfileDto toUserProfileDto(User user) {
        if (user == null) {
            return null;
        }

        List<String> roleNames = user.getRoles() == null
                ? List.of()
                : user.getRoles().stream().map(role -> role.getName()).toList();

        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getProfilePicture(),
                user.getGender(),
                roleNames
        );
    }
}
