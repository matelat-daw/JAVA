package com.futureprograms.MyIkea.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/images")
@Slf4j
public class ImageFallbackController {

    private static final String DEFAULT_IMAGE = "default.jpg";

    /**
     * ✅ Maneja solicitudes de imágenes sin mostrar errores
     * Si la imagen no existe, devuelve la imagen por defecto
     */
    @GetMapping("/{filename:.+}")
    @ResponseBody
    public Resource getImage(@PathVariable String filename) {
        try {
            // ✅ Validar que el filename no contenga caracteres peligrosos
            if (filename == null || filename.isEmpty() || filename.contains("..")) {
                log.debug("Filename inválido solicitado: {}", filename);
                return getDefaultImage();
            }

            // ✅ Intentar cargar desde classpath
            Resource image = new ClassPathResource("static/images/" + filename);
            
            if (image.exists() && image.isReadable()) {
                log.debug("Imagen encontrada: {}", filename);
                return image;
            } else {
                log.debug("Imagen no encontrada: {}, usando imagen por defecto sin error", filename);
                return getDefaultImage();
            }
            
        } catch (Exception e) {
            // ✅ No registrar como error, solo como debug para evitar logs innecesarios
            log.debug("No se pudo cargar imagen: {}, usando por defecto", filename);
            return getDefaultImage();
        }
    }

    /**
     * ✅ Devuelve la imagen por defecto sin generar excepciones
     */
    private Resource getDefaultImage() {
        try {
            return new ClassPathResource("static/images/" + DEFAULT_IMAGE);
        } catch (Exception e) {
            log.warn("Advertencia: No se pudo cargar la imagen por defecto: {}", e.getMessage());
            throw new RuntimeException("Imagen por defecto no disponible", e);
        }
    }
}
