package com.futureprograms.authapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;
import com.futureprograms.authapi.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controlador para servir imágenes subidas por usuarios
 * Maneja la descarga de imágenes de perfil y otros archivos
 */
@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = {"http://localhost"}, allowCredentials = "true")
@Slf4j
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * Obtiene una imagen por nombre (soporta subcarpetas de usuario como {userId}/profile.jpg)
     * GET /api/images/**
     */
    @GetMapping("/**")
    public ResponseEntity<?> getImage(HttpServletRequest request) {
        // En Spring, si RequestMapping es /api/images y pedimos /api/images/2/profile.jpg
        // PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE suele devolver /2/profile.jpg (la parte variable)
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        
        // Limpiar la ruta para obtener el nombre relativo real
        String fileName = path;
        // Eliminar cualquier prefijo conocido que pueda venir de la URL o la base de datos
        fileName = fileName.replace("/api/images/", "");
        if (fileName.startsWith("/")) fileName = fileName.substring(1);
        if (fileName.startsWith("images/")) fileName = fileName.substring(7);
        
        // Nueva limpieza: si el nombre viene como "2/profile.jpg" o similar, asegurar que no tenga dobles slashes
        fileName = fileName.replace("//", "/");

        if (fileName.isEmpty() || fileName.equals("health")) {
             return health();
        }

        log.info("🖼️ Solicitud de imagen procesada. Archivo buscado: '{}'", fileName);
        try {
            // 1. Intentar cargar desde el directorio físico configurado (upload.dir)
            String uploadDirProperty = imageService.getUploadDir();
            Path uploadPath = Paths.get(uploadDirProperty).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(fileName).normalize();
            
            log.info("📂 Intento 1: Buscando en {}", filePath.toAbsolutePath());

            if (Files.exists(filePath) && Files.isReadable(filePath)) {
                log.info("✅ Archivo encontrado en Intento 1");
                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists() && resource.isReadable()) {
                    return serveResource(resource, filePath);
                }
            }

            // 2. Si no funciona, intentar desde el directorio de clases (target/classes/static/images)
            String userDir = System.getProperty("user.dir");
            Path classesPath = Paths.get(userDir, "target", "classes", "static", "images", fileName).normalize();
            log.info("📂 Intento 2: Buscando en {}", classesPath.toAbsolutePath());
            
            if (Files.exists(classesPath) && Files.isReadable(classesPath)) {
                log.info("✅ Archivo encontrado en Intento 2");
                Resource resource = new UrlResource(classesPath.toUri());
                if (resource.exists() && resource.isReadable()) {
                    return serveResource(resource, classesPath);
                }
            }

            // 3. Si no está en disco (ej: male.png recién compilado), buscar en el classpath interno
            log.info("📂 Intento 3: Buscando en classpath: static/images/{}", fileName);
            org.springframework.core.io.ClassPathResource classPathResource = 
                new org.springframework.core.io.ClassPathResource("static/images/" + fileName);
            
            if (classPathResource.exists() && classPathResource.isReadable()) {
                log.info("✅ Archivo encontrado en Intento 3");
                return serveResource(classPathResource, Paths.get(classPathResource.getURI()));
            }

            log.warn("❌ ARCHIVO NO ENCONTRADO EN NINGUNA RUTA: {}", fileName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Imagen no encontrada: " + fileName);

        } catch (Exception e) {
            log.error("❌ Error crítico al obtener imagen: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la imagen: " + e.getMessage());
        }
    }

    private ResponseEntity<Resource> serveResource(Resource resource, Path path) throws Exception {
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        log.info("✅ Sirviendo recurso: {} ({})", path.getFileName(), contentType);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
                .body(resource);
    }

    /**
     * Health check para servicio de imágenes
     * GET /api/images/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok()
                .body("{\"status\": \"Image service is running\", \"upload_dir\": \"" + imageService.getUploadDir() + "\"}");
    }
}
