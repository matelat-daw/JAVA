package com.futureprograms.authapi.service;

import com.futureprograms.authapi.enums.Gender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class ImageService {

    @Value("${upload.dir:target/classes/static/images}")
    private String uploadDir;

    public String getUploadDir() {
        // The uploadDir property from application.properties
        return uploadDir;
    }

    /**
     * Verifica si una imagen es protegida (por defecto según género)
     */
    public boolean isProtectedImage(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return false;
        }
        return Gender.isDefaultImagePath(imagePath);
    }

    /**
     * Obtiene la ruta de imagen por defecto para un género
     */
    public String getDefaultImagePath(String genderDisplayName) {
        try {
            Gender gender = Gender.fromDisplayName(genderDisplayName);
            return gender.getDefaultImagePath();
        } catch (IllegalArgumentException e) {
            log.error("Gender no válido: {}", genderDisplayName);
            throw new RuntimeException("Género no válido");
        }
    }

    /**
     * Elimina una imagen (solo si no es protegida)
     */
    public boolean deleteImage(String imagePath) {
        // Validar que no es imagen protegida
        if (isProtectedImage(imagePath)) {
            log.warn("Intento de eliminar imagen protegida: {}", imagePath);
            throw new RuntimeException("No se pueden eliminar imágenes por defecto");
        }

        try {
            Path path = Paths.get(uploadDir, imagePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Imagen eliminada: {}", imagePath);
                return true;
            }
            log.warn("Imagen no encontrada: {}", imagePath);
            return false;
        } catch (Exception e) {
            log.error("Error al eliminar imagen: {}", imagePath, e);
            throw new RuntimeException("Error al eliminar imagen: " + e.getMessage());
        }
    }

    /**
     * Verifica si un archivo de imagen existe
     */
    public boolean imageExists(String imagePath) {
        try {
            Path path = Paths.get(uploadDir, imagePath);
            return Files.exists(path);
        } catch (Exception e) {
            log.error("Error al verificar imagen: {}", imagePath, e);
            return false;
        }
    }

    /**
     * Valida si una ruta es válida (no contiene caracteres peligrosos)
     */
    public boolean isValidImagePath(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return false;
        }
        // Evitar path traversal
        return !imagePath.contains("..") && !imagePath.contains("//") && 
               imagePath.matches("^[a-zA-Z0-9/_.-]+$");
    }

    /**
     * Obtiene el nombre de archivo de una ruta
     */
    public String getImageFileName(String imagePath) {
        if (imagePath == null) {
            return null;
        }
        return imagePath.substring(imagePath.lastIndexOf("/") + 1);
    }

    /**
     * Genera una ruta segura para una imagen
     */
    public String generateSecureImagePath(String fileName, Long userId) {
        // Validar nombre de archivo
        if (!fileName.matches("^[a-zA-Z0-9._-]+$")) {
            throw new RuntimeException("Nombre de archivo inválido");
        }

        // Crear carpeta por usuario
        String userImageDir = String.format("users/%d/", userId);
        return userImageDir + System.currentTimeMillis() + "_" + fileName;
    }

    /**
     * Guarda una imagen de perfil de usuario con nombre fijo "profile"
     */
    public String saveProfileImage(MultipartFile file, Long userId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("El archivo de imagen está vacío");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (!isValidImageType(contentType)) {
            throw new RuntimeException("Tipo de archivo no permitido. Solo se permiten imágenes (jpg, png, gif, webp)");
        }

        // Obtener extensión original
        String originalFilename = file.getOriginalFilename();
        String extension = "jpg"; // default
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        try {
            // Crear directorio del usuario si no existe
            Path userPath = Paths.get(uploadDir, String.valueOf(userId));
            if (!Files.exists(userPath)) {
                Files.createDirectories(userPath);
            }

            // El nombre siempre será "profile" con su extensión original
            String fileName = "profile." + extension;
            Path filePath = userPath.resolve(fileName);

            // Guardar archivo (sobrescribirá si ya existe)
            Files.write(filePath, file.getBytes());
            
            // La ruta que guardamos en BD será relativa a uploadDir: "ID/profile.ext"
            String dbPath = userId + "/" + fileName;
            log.info("📸 Imagen de perfil guardada para usuario {}: {}", userId, dbPath);
            
            return dbPath;
        } catch (Exception e) {
            log.error("❌ Error al guardar imagen para usuario {}: {}", userId, e.getMessage());
            throw new RuntimeException("Error al guardar imagen: " + e.getMessage());
        }
    }

    /**
     * Guarda una imagen de perfil de usuario (MÉTODO ANTIGUO - DEPRECADO)
     */
    @Deprecated
    public String saveProfileImage(MultipartFile file) throws Exception {
        return saveProfileImage(file, System.currentTimeMillis());
    }

    /**
     * Elimina la imagen de perfil de un usuario específico
     * - Si es imagen protegida (default), no elimina nada
     * - Si es imagen personalizada, elimina todo el directorio del usuario
     */
    public void deleteProfileImage(Long userId, String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            log.warn("Ruta de imagen vacía para usuario: {}", userId);
            return;
        }

        // Si es imagen protegida, no hacer nada
        if (isProtectedImage(imagePath)) {
            log.info("📸 Imagen protegida (por defecto) para usuario {}. No se eliminará.", userId);
            return;
        }

        // Eliminar toda la carpeta del usuario
        deleteUserProfileImages(userId);
    }

    /**
     * Elimina todos los archivos de perfil de un usuario (carpeta completa)
     * Se usa cuando se elimina la cuenta del usuario
     */
    public void deleteUserProfileImages(Long userId) {
        try {
            Path userPath = Paths.get(uploadDir, userId.toString());
            
            if (!Files.exists(userPath)) {
                log.warn("📁 Carpeta de usuario no encontrada: {}", userPath);
                return;
            }

            // Eliminar recursivamente toda la carpeta
            Files.walk(userPath)
                    .sorted((a, b) -> b.compareTo(a)) // Ordenar en reverso para eliminar primero archivos
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("✅ Eliminado: {}", path);
                        } catch (Exception e) {
                            log.warn("⚠️ No se pudo eliminar: {} - {}", path, e.getMessage());
                        }
                    });

            log.info("✅ Carpeta de imágenes del usuario {} eliminada completamente", userId);
        } catch (Exception e) {
            log.error("❌ Error al eliminar carpeta de imágenes del usuario {}: {}", userId, e.getMessage());
            // No lanzar excepción, solo logging para no interrumpir la eliminación del usuario
        }
    }

    /**
     * Valida el tipo de contenido de la imagen
     */
    private boolean isValidImageType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }
}
