package com.futureprograms.MyIkea.Services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Servicio para manejar la carga de archivos (imágenes de productos)
 * Guarda los archivos en la carpeta static/images que Spring sirve automáticamente
 * De la misma forma que se cargan con @{'/images/' + ...} en los templates
 */
@Service
public class FileUploadService {
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};

    /**
     * Obtiene la ruta del directorio images de forma dinámica
     * Busca la carpeta correcta en múltiples ubicaciones
     */
    private Path getUploadDirectory() throws IOException {
        String workingDir = System.getProperty("user.dir");
        System.out.println("Directorio de trabajo: " + workingDir);
        System.out.flush();
        
        // Intenta 1: Desde el directorio de trabajo actual + MyIkea
        Path imagesPath = Paths.get(workingDir, "MyIkea", "src", "main", "resources", "static", "images");
        File checkDir = new File(imagesPath.toString());
        
        System.out.println("Intento 1: " + imagesPath.toString());
        System.out.println("  ¿Existe? " + checkDir.exists());
        System.out.flush();
        
        if (checkDir.exists() && checkDir.isDirectory()) {
            System.out.println("✓ Directorio encontrado en Intento 1");
            System.out.flush();
            return imagesPath.toAbsolutePath();
        }
        
        // Intenta 2: Desde el directorio de trabajo sin MyIkea
        imagesPath = Paths.get(workingDir, "src", "main", "resources", "static", "images");
        checkDir = new File(imagesPath.toString());
        
        System.out.println("Intento 2: " + imagesPath.toString());
        System.out.println("  ¿Existe? " + checkDir.exists());
        System.out.flush();
        
        if (checkDir.exists() && checkDir.isDirectory()) {
            System.out.println("✓ Directorio encontrado en Intento 2");
            System.out.flush();
            return imagesPath.toAbsolutePath();
        }
        
        // Intenta 3: Ruta absoluta directa (debe funcionar siempre)
        imagesPath = Paths.get("d:/Server/html/MyIkea/src/main/resources/static/images");
        checkDir = new File(imagesPath.toString());
        
        System.out.println("Intento 3: " + imagesPath.toString());
        System.out.println("  ¿Existe? " + checkDir.exists());
        System.out.flush();
        
        if (checkDir.exists() && checkDir.isDirectory()) {
            System.out.println("✓ Directorio encontrado en Intento 3");
            System.out.flush();
            return imagesPath.toAbsolutePath();
        }
        
        // Si ninguna funciona, usa la más probable y déjalo crear el directorio
        imagesPath = Paths.get(workingDir, "MyIkea", "src", "main", "resources", "static", "images");
        System.out.println("❌ Ninguna ruta existente encontrada.");
        System.out.println("Usaré esta ruta: " + imagesPath.toAbsolutePath());
        System.out.flush();
        
        return imagesPath.toAbsolutePath();
    }

    /**
     * Guarda un archivo de imagen en el servidor con su nombre original
     * @param file archivo a guardar
     * @return nombre del archivo guardado
     * @throws IOException si hay error al guardar el archivo
     * @throws IllegalArgumentException si el archivo no es válido
     */
    public String saveImage(MultipartFile file) throws IOException {
        System.out.println("\n========== INICIANDO GUARDADO DE IMAGEN ==========");
        System.out.flush();
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        String originalFileName = file.getOriginalFilename();
        System.out.println("Nombre original del archivo: " + originalFileName);
        System.out.println("Tamaño del archivo: " + file.getSize() + " bytes");
        System.out.flush();

        // Validar tamaño
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo es demasiado grande. Máximo 10MB");
        }

        // Validar extensión
        if (originalFileName == null || !isValidExtension(originalFileName)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Solo se aceptan: jpg, jpeg, png, gif, webp");
        }

        try {
            // Obtener el directorio de carga de forma dinámica
            Path uploadPath = getUploadDirectory();
            System.out.println("Ruta absoluta del directorio: " + uploadPath);
            System.out.println("Ruta en String: " + uploadPath.toString());
            System.out.flush();

            // Verificar si el directorio existe
            File dir = uploadPath.toFile();
            System.out.println("¿Directorio existe? " + dir.exists());
            System.out.println("¿Es directorio? " + dir.isDirectory());
            System.out.println("Ruta absoluta del File: " + dir.getAbsolutePath());
            System.out.flush();
            
            if (!dir.exists()) {
                System.out.println("Directorio no existe, intentando crear: " + uploadPath);
                System.out.flush();
                
                if (!dir.mkdirs()) {
                    String error = "❌ NO se pudo crear el directorio: " + uploadPath;
                    System.out.println(error);
                    System.out.flush();
                    throw new IOException(error);
                }
                System.out.println("✓ Directorio creado exitosamente");
                System.out.flush();
            } else {
                System.out.println("✓ Directorio ya existe");
                System.out.flush();
            }

            // Verificar permisos de lectura y escritura
            System.out.println("Verificando permisos...");
            System.out.println("  - ¿Puede leer? " + dir.canRead());
            System.out.println("  - ¿Puede escribir? " + dir.canWrite());
            System.out.println("  - ¿Puede ejecutar? " + dir.canExecute());
            System.out.flush();
            
            if (!dir.canWrite()) {
                String error = "❌ NO hay permisos de escritura en: " + uploadPath;
                System.out.println(error);
                System.out.flush();
                throw new IOException(error);
            }
            System.out.println("✓ Permisos de escritura verificados");
            System.out.flush();

            // Usar el nombre original del archivo
            Path filePath = uploadPath.resolve(originalFileName);
            System.out.println("Ruta completa del archivo: " + filePath);
            System.out.println("Ruta absoluta: " + filePath.toAbsolutePath());
            System.out.flush();
            
            // Copiar archivo a disco
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✓ Archivo copiado al disco");
            System.out.flush();

            // Verificar que se guardó correctamente
            File savedFile = new File(filePath.toString());
            if (savedFile.exists()) {
                long fileSize = savedFile.length();
                System.out.println("✓ Archivo guardado exitosamente");
                System.out.println("  - Nombre: " + originalFileName);
                System.out.println("  - Ruta: " + filePath.toAbsolutePath());
                System.out.println("  - Tamaño en disco: " + fileSize + " bytes");
                System.out.println("  - Se cargará en la página como: <img src=\"/images/" + originalFileName + "\">");
                System.out.println("========== GUARDADO COMPLETADO EXITOSAMENTE ==========\n");
                System.out.flush();
                return originalFileName;
            } else {
                String error = "❌ El archivo NO existe después de guardarlo en: " + filePath;
                System.out.println(error);
                System.out.flush();
                throw new IOException(error);
            }
            
        } catch (IOException e) {
            System.out.println("❌ Error al guardar archivo: " + e.getMessage());
            e.printStackTrace();
            System.out.flush();
            throw new IOException("Error al guardar el archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un archivo de imagen del servidor
     * PROTEGE las imágenes por defecto de ser eliminadas
     * @param fileName nombre del archivo a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean deleteImage(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        // Proteger las imágenes por defecto - NUNCA ELIMINARLAS
        String[] protectedImages = {"female.png", "male.png", "other.png", "default.jpg"};
        for (String protectedImage : protectedImages) {
            if (fileName.equalsIgnoreCase(protectedImage)) {
                System.out.println("⚠️  Intento de eliminar imagen protegida: " + fileName);
                System.out.println("    Esta es una imagen por defecto y no se elimina");
                System.out.flush();
                return false; // No eliminar, pero retornar false (no es error, simplemente no se hace)
            }
        }

        try {
            Path uploadPath = getUploadDirectory();
            Path filePath = uploadPath.resolve(fileName);
            File file = new File(filePath.toString());
            
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("✓ Archivo eliminado: " + filePath);
                    System.out.flush();
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error al eliminar archivo: " + e.getMessage());
            System.out.flush();
        }

        return false;
    }

    /**
     * Verifica si la extensión del archivo es válida
     */
    private boolean isValidExtension(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (extension.equals(allowed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene la extensión de un archivo
     */
    private String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot > 0) {
            return fileName.substring(lastIndexOfDot + 1);
        }
        return "";
    }
}
