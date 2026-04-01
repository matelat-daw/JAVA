package com.futureprograms.MyIkea.Api.Controllers;

import com.futureprograms.MyIkea.Api.Dto.MunicipalityDto;
import com.futureprograms.MyIkea.Api.Dto.ProductDto;
import com.futureprograms.MyIkea.Api.Dto.ProvinceDto;
import com.futureprograms.MyIkea.Api.Mapper.ApiMapper;
import com.futureprograms.MyIkea.Services.LocationService;
import com.futureprograms.MyIkea.Services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
public class PublicCatalogApiController {

    private final ProductService productService;
    private final LocationService locationService;

    public PublicCatalogApiController(ProductService productService, LocationService locationService) {
        this.productService = productService;
        this.locationService = locationService;
    }

    @GetMapping("/products")
    public List<ProductDto> getProducts() {
        return productService.getAllProducts().stream()
                .map(ApiMapper::toProductDto)
                .toList();
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Integer id) {
        return productService.getProductById(id)
                .map(ApiMapper::toProductDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/provinces")
    public List<ProvinceDto> getProvinces() {
        return locationService.getAllProvinces().stream()
                .map(ApiMapper::toProvinceDto)
                .toList();
    }

    @GetMapping("/municipalities")
    public List<MunicipalityDto> getMunicipalities(@RequestParam(required = false) Integer provinceId) {
        if (provinceId == null) {
            return locationService.getAllMunicipalities().stream()
                    .map(ApiMapper::toMunicipalityDto)
                    .toList();
        }

        return locationService.getMunicipalitiesByProvinceId(provinceId).stream()
                .map(ApiMapper::toMunicipalityDto)
                .toList();
    }
}
