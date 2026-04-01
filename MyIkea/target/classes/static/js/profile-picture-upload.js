// Script para subida de foto de perfil
// Controla la carga de archivos de forma AJAX compatible con SPA

document.addEventListener('DOMContentLoaded', function() {
    console.log('[ProfilePictureUpload] DOMContentLoaded - inicializando');
    initProfilePictureUpload();
}, { once: true });

// También ejecutar si el script se carga después de DOMContentLoaded (SPA)
setTimeout(() => {
    if (document.readyState !== 'loading' && !document.body.dataset.profilePictureInitialized) {
        console.log('[ProfilePictureUpload] Post-DOMContentLoaded - inicializando');
        initProfilePictureUpload();
    }
}, 100);

function initProfilePictureUpload() {
    // Prevenir múltiples inicializaciones
    if (document.body.dataset.profilePictureInitialized === 'true') {
        console.log('[ProfilePictureUpload] Ya inicializado en esta página');
        return;
    }
    
    const picInput = document.getElementById('picInput');
    const submitBtn = document.getElementById('submitBtn');
    const uploadForm = document.querySelector('form[action="/profile/update-picture"]');
    
    if (!picInput || !submitBtn || !uploadForm) {
        console.warn('[ProfilePictureUpload] Elementos no encontrados:', {
            picInput: !!picInput,
            submitBtn: !!submitBtn,
            uploadForm: !!uploadForm
        });
        return;
    }
    
    console.log('[ProfilePictureUpload] Inicializando upload de foto');
    document.body.dataset.profilePictureInitialized = 'true';
    
    // Mostrar/ocultar botón de envío cuando se selecciona una foto
    picInput.addEventListener('change', function() {
        if (this.files && this.files[0]) {
            console.log('[ProfilePictureUpload] Foto seleccionada:', this.files[0].name);
            submitBtn.classList.remove('d-none');
        }
    });
    
    // Interceptar el envío del formulario
    uploadForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const file = picInput.files[0];
        if (!file) {
            console.warn('[ProfilePictureUpload] No hay archivo seleccionado');
            return;
        }
        
        console.log('[ProfilePictureUpload] Iniciando upload:', file.name);
        
        // Deshabilitar botón durante envío
        submitBtn.disabled = true;
        const originalText = submitBtn.innerHTML;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Subiendo...';
        
        // Crear FormData con el archivo y token CSRF
        const formData = new FormData();
        formData.append('profilePicture', file);
        
        const csrfToken = uploadForm.querySelector('input[name="_csrf"]');
        if (csrfToken) {
            formData.append('_csrf', csrfToken.value);
        }
        
        // Enviar via fetch al endpoint AJAX
        fetch('/profile/update-picture-ajax', {
            method: 'POST',
            body: formData,
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => {
            console.log('[ProfilePictureUpload] Response status:', response.status);
            
            if (!response.ok) {
                throw new Error('Error en la respuesta del servidor: ' + response.status);
            }
            
            return response.json();
        })
        .then(data => {
            console.log('[ProfilePictureUpload] Response data:', data);
            
            if (data.success) {
                console.log('[ProfilePictureUpload] ✓ Foto cargada exitosamente');
                
                // Actualizar la URL de la imagen en la pantalla
                const currentImg = document.querySelector('img[alt="Foto"]');
                if (currentImg && data.imageUrl) {
                    currentImg.src = data.imageUrl + '?t=' + new Date().getTime();
                    console.log('[ProfilePictureUpload] Imagen actualizada en la pantalla');
                }
                
                // Mostrar mensaje de éxito
                showUploadSuccess(data.message);
                
                // Resetear formulario
                picInput.value = '';
                submitBtn.classList.add('d-none');
            } else {
                throw new Error(data.message || 'Error desconocido');
            }
        })
        .catch(error => {
            console.error('[ProfilePictureUpload] ✗ Error:', error);
            showUploadError('Error al subir la foto: ' + error.message);
        })
        .finally(() => {
            // Restaurar botón
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        });
    });
    
    console.log('[ProfilePictureUpload] ✓ Inicializado correctamente');
}

/**
 * Muestra un mensaje de éxito
 */
function showUploadSuccess(message) {
    console.log('[ProfilePictureUpload] Mostrando éxito:', message);
    
    // Buscar o crear el div de éxito
    const profileCard = document.querySelector('form[action="/profile/update-picture"]').closest('.card-body');
    let successDiv = profileCard?.querySelector('.alert-success');
    
    if (!successDiv) {
        successDiv = document.createElement('div');
        successDiv.className = 'alert alert-success alert-sm py-2 mt-2';
        if (profileCard) {
            profileCard.appendChild(successDiv);
        }
    }
    
    const small = successDiv.querySelector('small') || document.createElement('small');
    small.textContent = message;
    
    if (!successDiv.querySelector('small')) {
        successDiv.appendChild(small);
    }
    
    successDiv.style.display = 'block';
    
    // Remover div de error si existe
    const errorDiv = profileCard?.querySelector('.alert-danger');
    if (errorDiv) {
        errorDiv.style.display = 'none';
    }
    
    // Auto-ocultar después de 3 segundos
    setTimeout(() => {
        successDiv.style.display = 'none';
    }, 3000);
}

/**
 * Muestra un mensaje de error
 */
function showUploadError(message) {
    console.error('[ProfilePictureUpload] Mostrando error:', message);
    
    // Buscar o crear el div de error
    const profileCard = document.querySelector('form[action="/profile/update-picture"]').closest('.card-body');
    let errorDiv = profileCard?.querySelector('.alert-danger');
    
    if (!errorDiv) {
        errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-danger alert-sm py-2 mt-2';
        if (profileCard) {
            profileCard.appendChild(errorDiv);
        }
    }
    
    const small = errorDiv.querySelector('small') || document.createElement('small');
    small.textContent = message;
    
    if (!errorDiv.querySelector('small')) {
        errorDiv.appendChild(small);
    }
    
    errorDiv.style.display = 'block';
    
    // Remover div de éxito si existe
    const successDiv = profileCard?.querySelector('.alert-success');
    if (successDiv) {
        successDiv.style.display = 'none';
    }
}

// Hacer funciones globales accesibles
window.initProfilePictureUpload = initProfilePictureUpload;
window.showUploadSuccess = showUploadSuccess;
window.showUploadError = showUploadError;
