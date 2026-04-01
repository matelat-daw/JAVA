package com.futureprograms.authapi.enums;

public enum Gender {
    MALE("Varón", "default/male.png"),
    FEMALE("Mujer", "default/female.png"),
    OTHER("Otro", "default/other.png");

    private final String displayName;
    private final String defaultImage;

    Gender(String displayName, String defaultImage) {
        this.displayName = displayName;
        this.defaultImage = defaultImage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultImage() {
        return defaultImage;
    }

    public String getDefaultImagePath() {
        return defaultImage;
    }

    /**
     * Obtiene el enum desde el display name
     */
    public static Gender fromDisplayName(String displayName) {
        for (Gender g : Gender.values()) {
            if (g.displayName.equalsIgnoreCase(displayName)) {
                return g;
            }
        }
        throw new IllegalArgumentException("Gender no válido: " + displayName);
    }

    /**
     * Obtiene el enum desde el nombre en inglés (male, female, other)
     */
    public static Gender fromEnglishName(String englishName) {
        try {
            return Gender.valueOf(englishName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Gender no válido: " + englishName);
        }
    }

    /**
     * Verifica si es una imagen por defecto (protegida)
     */
    public static boolean isDefaultImage(String imageName) {
        if (imageName == null) return false;
        for (Gender g : Gender.values()) {
            // Comparar con el nombre del archivo extraído de la ruta completa
            String defaultFileName = g.defaultImage.contains("/") 
                ? g.defaultImage.substring(g.defaultImage.lastIndexOf("/") + 1) 
                : g.defaultImage;
            if (defaultFileName.equalsIgnoreCase(imageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si el path es de una imagen por defecto
     */
    public static boolean isDefaultImagePath(String path) {
        if (path == null) return false;
        // Normalizar path para comparar
        String fileName = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;
        return isDefaultImage(fileName);
    }
}
