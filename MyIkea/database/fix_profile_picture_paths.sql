-- Script para limpiar las rutas de imágenes de perfil
-- Reemplaza /images/filename.png con solo filename.png

UPDATE users 
SET profile_picture = SUBSTRING(profile_picture, 9) 
WHERE profile_picture LIKE '/images/%';

-- Verificación
SELECT id, username, email, profile_picture FROM users WHERE profile_picture IS NOT NULL;
