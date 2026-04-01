package com.futureprograms.MyIkea.Controllers;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.futureprograms.MyIkea.Models.Product;
import com.futureprograms.MyIkea.Services.FileUploadService;
import com.futureprograms.MyIkea.Services.LocationService;
import com.futureprograms.MyIkea.Services.ProductService;
import com.futureprograms.MyIkea.Constants.AppConstants;
import com.futureprograms.MyIkea.Utils.AuthUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Controller
@RequestMapping("/products")
@Slf4j
public class ProductsController {
    private final ProductService productService;
    private final LocationService locationService;
    private final FileUploadService fileUploadService;

    public ProductsController(ProductService productService, LocationService locationService, FileUploadService fileUploadService) {
        this.productService = productService;
        this.locationService = locationService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping
    public String listProducts(Model model, Authentication authentication) {
        boolean isAdmin = AuthUtils.hasRole(authentication, AppConstants.ROLE_ADMIN);
        boolean isManagerOrAdmin = AuthUtils.hasAnyRole(authentication, AppConstants.ROLE_MANAGER, AppConstants.ROLE_ADMIN);

        List<Product> productos = productService.getAllProducts();
        model.addAttribute("productos", productos);
        model.addAttribute("ADMIN", isAdmin);
        model.addAttribute("CAN_CREATE", isManagerOrAdmin);

        return "products/index";
    }

    @GetMapping("/details/{id}")
    public String detailProduct(@PathVariable Integer id, Model model) {
        productService.getProductById(id).ifPresentOrElse(
                product -> model.addAttribute("producto", product),
                () -> model.addAttribute("error", "Producto no encontrado")
        );
        return "products/details";
    }

    @GetMapping("/create")
    public String createProduct(Model model, Authentication authentication) {
        if (!AuthUtils.hasAnyRole(authentication, AppConstants.ROLE_MANAGER, AppConstants.ROLE_ADMIN)) {
            log.warn("Intento no autorizado de acceso a crear producto por usuario: {}", authentication.getName());
            return "redirect:/products";
        }

        prepareModelForProduct(model, new Product());
        return "products/create";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute Product producto, 
            BindingResult result,
            @RequestParam(value = "productPictureFile", required = false) MultipartFile file,
            Model model, 
            Authentication authentication) {
        
        log.info("Procesando creación de producto: {}", producto.getProductName());
        
        if (!AuthUtils.hasAnyRole(authentication, AppConstants.ROLE_MANAGER, AppConstants.ROLE_ADMIN)) {
            log.warn("Intento no autorizado de crear producto por usuario: {}", authentication.getName());
            model.addAttribute("error", "No tienes permiso para crear productos.");
            prepareModelForProduct(model, producto);
            return "products/create";
        }

        if (result.hasErrors()) {
            log.warn("Errores de validación al crear producto: {}", result.getAllErrors());
            prepareModelForProduct(model, producto);
            return "products/create";
        }

        MultipartFile fileToProcess = (file != null && !file.isEmpty()) ? file : producto.getProductPictureFile();

        try {
            if (fileToProcess != null && !fileToProcess.isEmpty()) {
                String fileName = fileUploadService.saveImage(fileToProcess);
                producto.setProductPicture(fileName);
                log.info("Imagen guardada exitosamente: {}", fileName);
            }

            productService.saveProduct(producto);
            log.info("Producto creado exitosamente por {}: {}", authentication.getName(), producto.getProductName());
            return "redirect:/products";
            
        } catch (IllegalArgumentException e) {
            log.warn("Validación de archivo fallida: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            prepareModelForProduct(model, producto);
            return "products/create";
            
        } catch (Exception e) {
            log.error("Error al guardar producto: ", e);
            model.addAttribute("error", "Error al guardar el producto.");
            prepareModelForProduct(model, producto);
            return "products/create";
        }
    }

    private void prepareModelForProduct(Model model, Product product) {
        model.addAttribute("producto", product);
        model.addAttribute("provincias", locationService.getAllProvinces());
        model.addAttribute("municipios", locationService.getAllMunicipalities());
    }
}