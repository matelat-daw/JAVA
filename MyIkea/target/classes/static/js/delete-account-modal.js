// Modal de Eliminación de Cuenta
// Maneja toda la lógica del modal para eliminar la cuenta del usuario

function initDeleteAccountModal() {
    console.log('[DeleteAccount] Inicializando modal de eliminación de cuenta');
    
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    
    if (!confirmDeleteBtn) {
        console.warn('[DeleteAccount] Botón confirmDeleteBtn no encontrado');
        return;
    }
    
    // Remover cualquier listener anterior para evitar duplicados
    const newBtn = confirmDeleteBtn.cloneNode(true);
    confirmDeleteBtn.parentNode.replaceChild(newBtn, confirmDeleteBtn);
    
    const deleteForm = document.getElementById('deleteForm');
    const delPassword = document.getElementById('delPassword');
    const deleteModal = document.getElementById('deleteModal');
    const goodbyeModal = document.getElementById('goodbyeModal');
    const emptyPasswordModal = document.getElementById('emptyPasswordModal');
    const confirmationModal = document.getElementById('confirmationModal');
    const credentialsErrorModal = document.getElementById('credentialsErrorModal');
    const invalidResponseModal = document.getElementById('invalidResponseModal');
    const connectionErrorModal = document.getElementById('connectionErrorModal');
    const confirmDeleteFinalBtn = document.getElementById('confirmDeleteFinalBtn');
    
    console.log('[DeleteAccount] Elementos encontrados:', {
        deleteForm: !!deleteForm,
        delPassword: !!delPassword,
        deleteModal: !!deleteModal,
        goodbyeModal: !!goodbyeModal,
        confirmDeleteBtn: !!newBtn,
        confirmDeleteFinalBtn: !!confirmDeleteFinalBtn
    });
    
    if (!deleteForm || !delPassword || !deleteModal || !confirmDeleteFinalBtn) {
        console.error('[DeleteAccount] Faltan elementos del modal de eliminación');
        return;
    }
    
    // Agregar listener al botón de eliminar
    newBtn.addEventListener('click', async function(e) {
        e.preventDefault();
        console.log('[DeleteAccount] Click en botón Sí, eliminar');
        
        const password = delPassword.value.trim();
        
        if (!password) {
            console.warn('[DeleteAccount] Contraseña vacía');
            // Mostrar modal de contraseña vacía
            if (emptyPasswordModal) {
                const modal = new bootstrap.Modal(emptyPasswordModal);
                modal.show();
            }
            delPassword.focus();
            return;
        }
        
        console.log('[DeleteAccount] Mostrando modal de confirmación final');
        // Mostrar modal de confirmación adicional
        if (deleteModal && confirmationModal) {
            const deleteModalInstance = bootstrap.Modal.getInstance(deleteModal);
            if (deleteModalInstance) {
                deleteModalInstance.hide();
            }
            const confirmModalInstance = new bootstrap.Modal(confirmationModal, { backdrop: true, keyboard: true });
            confirmModalInstance.show();
        }
    });
    
    // Listener para el botón de confirmación final
    if (confirmDeleteFinalBtn) {
        confirmDeleteFinalBtn.addEventListener('click', async function(e) {
            e.preventDefault();
            console.log('[DeleteAccount] Confirmación final - iniciando eliminación');
            
            // Deshabilitar el botón
            confirmDeleteFinalBtn.disabled = true;
            const originalText = confirmDeleteFinalBtn.innerHTML;
            confirmDeleteFinalBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Eliminando...';
            
            try {
                console.log('[DeleteAccount] Iniciando eliminación de cuenta...');
                
                // Obtener el token CSRF
                const csrfInput = deleteForm.querySelector('input[name="_csrf"]');
                const csrfToken = csrfInput ? csrfInput.value : '';
                
                console.log('[DeleteAccount] Token CSRF obtenido:', !!csrfToken);
                
                const requestData = {
                    password: delPassword.value.trim()
                };
                
                console.log('[DeleteAccount] Enviando petición a /profile/delete-ajax');
                
                // Enviar solicitud al servidor
                const response = await fetch('/profile/delete-ajax', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': csrfToken
                    },
                    body: JSON.stringify(requestData)
                });
                
                console.log('[DeleteAccount] Respuesta recibida:', response.status, response.statusText);
                
                let result;
                try {
                    result = await response.json();
                    console.log('[DeleteAccount] Datos de respuesta:', result);
                } catch (parseError) {
                    console.error('[DeleteAccount] Error al parsear respuesta JSON:', parseError);
                    // Mostrar modal de respuesta inválida
                    if (confirmationModal) {
                        const modalInstance = bootstrap.Modal.getInstance(confirmationModal);
                        if (modalInstance) {
                            modalInstance.hide();
                        }
                    }
                    if (invalidResponseModal) {
                        const modal = new bootstrap.Modal(invalidResponseModal);
                        modal.show();
                    }
                    confirmDeleteFinalBtn.disabled = false;
                    confirmDeleteFinalBtn.innerHTML = originalText;
                    return;
                }
                
                if (response.ok && result.success) {
                    // Éxito - Mostrar modal de despedida
                    console.log('[DeleteAccount] Cuenta eliminada exitosamente');
                    
                    // Cerrar modal de confirmación
                    if (confirmationModal) {
                        const modalInstance = bootstrap.Modal.getInstance(confirmationModal);
                        if (modalInstance) {
                            modalInstance.hide();
                        }
                    }
                    
                    // Mostrar modal de despedida
                    if (goodbyeModal) {
                        const goodbyeModalInstance = new bootstrap.Modal(goodbyeModal, { backdrop: 'static', keyboard: true });
                        goodbyeModalInstance.show();
                    }
                    
                    // Redirigir después de 2 segundos
                    setTimeout(() => {
                        window.location.href = '/login';
                    }, 2000);
                } else {
                    // Error - Mostrar modal de error
                    console.error('[DeleteAccount] Error del servidor:', result.message);
                    
                    if (confirmationModal) {
                        const modalInstance = bootstrap.Modal.getInstance(confirmationModal);
                        if (modalInstance) {
                            modalInstance.hide();
                        }
                    }
                    
                    // Mostrar modal de credenciales incorrectas
                    if (credentialsErrorModal) {
                        const modal = new bootstrap.Modal(credentialsErrorModal);
                        modal.show();
                    }
                    
                    // Restaurar el botón
                    confirmDeleteFinalBtn.disabled = false;
                    confirmDeleteFinalBtn.innerHTML = originalText;
                }
            } catch (error) {
                console.error('[DeleteAccount] Error eliminando cuenta:', error);
                console.error('[DeleteAccount] Stack:', error.stack);
                
                // Mostrar modal de error de conexión
                if (confirmationModal) {
                    const modalInstance = bootstrap.Modal.getInstance(confirmationModal);
                    if (modalInstance) {
                        modalInstance.hide();
                    }
                }
                
                if (connectionErrorModal) {
                    const errorDetailsEl = connectionErrorModal.querySelector('#connectionErrorDetails');
                    if (errorDetailsEl) {
                        errorDetailsEl.textContent = error.message || 'Error desconocido';
                    }
                    const modal = new bootstrap.Modal(connectionErrorModal);
                    modal.show();
                }
                
                // Restaurar el botón
                confirmDeleteFinalBtn.disabled = false;
                confirmDeleteFinalBtn.innerHTML = originalText;
            }
        });
    }
    
    // Limpiar cuando se cierra el modal principal
    deleteModal.addEventListener('hidden.bs.modal', function() {
        console.log('[DeleteAccount] Modal de confirmación cerrado, limpiando campos');
        if (delPassword) {
            delPassword.value = '';
        }
        if (newBtn && !newBtn.disabled) {
            newBtn.innerHTML = 'Sí, eliminar';
        }
    });
    
    console.log('[DeleteAccount] Modal inicializado correctamente');
}

// Inicializar cuando el DOM esté completamente listo
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initDeleteAccountModal);
} else {
    initDeleteAccountModal();
}

// También inicializar cuando el fragment se recarga (para SPA)
// Escuchar un evento personalizado que debe dispararse cuando se carga un nuevo fragment
document.addEventListener('fragmentLoaded', function(e) {
    if (e.detail && e.detail.name === 'profile') {
        console.log('[DeleteAccount] Fragment "profile" recargado, reinicializando...');
        initDeleteAccountModal();
    }
});
