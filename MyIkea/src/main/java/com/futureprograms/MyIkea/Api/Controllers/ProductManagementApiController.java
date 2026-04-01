package com.futureprograms.MyIkea.Api.Controllers;

import com.futureprograms.MyIkea.Api.Dto.CreateProductRequest;
import com.futureprograms.MyIkea.Api.Dto.ProductDto;
import com.futureprograms.MyIkea.Api.Mapper.ApiMapper;
import com.futureprograms.MyIkea.Models.Auth.User;
import com.futureprograms.MyIkea.Models.Municipality;
import com.futureprograms.MyIkea.Models.Product;
import com.futureprograms.MyIkea.Services.FileUploadService;
import com.futureprograms.MyIkea.Services.LocationService;
import com.futureprograms.MyIkea.Services.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductManagementApiController {

    private final ProductService productService;
    private final LocationService locationService;
    private final FileUploadService fileUploadService;

    public ProductManagementApiController(
            ProductService productService,
            LocationService locationService,
            FileUploadService fileUploadService
    ) {
        this.productService = productService;
        this.locationService = locationService;
        this.fileUploadService = fileUploadService;
    }

    @PostMapping
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        if (!canCreateProduct(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para crear productos"));
        }

        Municipality municipality = locationService.getMunicipalityById(request.municipalityId());
        if (municipality == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Municipio no encontrado"));
        }

        Product product = new Product();
        product.setProductName(request.name());
        product.setProductPrice(request.price());
        product.setProductPicture(request.picture());
        product.setProductStock(request.stock());
        product.setMunicipality(municipality);

        Product saved = productService.saveProduct(product);
        ProductDto response = ApiMapper.toProductDto(saved);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProductMultipart(
            @RequestParam("name") String name,
            @RequestParam("price") Float price,
            @RequestParam("stock") Integer stock,
            @RequestParam("municipalityId") Integer municipalityId,
            @RequestParam(value = "productPictureFile", required = false) MultipartFile productPictureFile,
            @RequestParam(value = "picture", required = false) String picture,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        if (!canCreateProduct(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para crear productos"));
        }

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre es obligatorio"));
        }

        if (price == null || price < 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "El precio debe ser mayor o igual a 0"));
        }

        if (stock == null || stock < 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "El stock debe ser mayor o igual a 0"));
        }

        Municipality municipality = locationService.getMunicipalityById(municipalityId);
        if (municipality == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Municipio no encontrado"));
        }

        Product product = new Product();
        product.setProductName(name.trim());
        product.setProductPrice(price);
        product.setProductStock(stock);
        product.setMunicipality(municipality);

        try {
            if (productPictureFile != null && !productPictureFile.isEmpty()) {
                String fileName = fileUploadService.saveImage(productPictureFile);
                product.setProductPicture(fileName);
            } else if (picture != null && !picture.isBlank()) {
                product.setProductPicture(picture.trim());
            }

            Product saved = productService.saveProduct(product);
            ProductDto response = ApiMapper.toProductDto(saved);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al crear producto"));
        }
    }

    private boolean canCreateProduct(User user) {
        return user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER")
                        || authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
