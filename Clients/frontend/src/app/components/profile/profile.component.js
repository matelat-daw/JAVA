/**
 * ProfileComponent.js - Componente de gestión de perfil de usuario
 * Maneja actualización de datos, contraseña, foto y eliminación de cuenta
 */

class ProfileComponent {
    constructor() {
        this.selector = '#router-outlet';
        this.api = '/api/profile'; // Using /api namespace
    }

    /**
     * Inicializa el componente
     */
    async init() {
        try {
            console.log('🔄 Inicializando componente de perfil...');

            if (!AuthService.isAuthenticated()) {
                App.getInstance().navigateTo('/login');
                return;
            }

            const response = await fetch('/frontend/src/app/components/profile/profile.component.html');
            const html = await response.text();

            const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = html;

                // Esperar a que Bootstrap esté disponible
                await this.waitForBootstrap();

                // Cargar datos del usuario
                await this.loadUserData();

                // Adjuntar eventos
                this.attachEventListeners();

                console.log('✅ Componente de perfil inicializado correctamente');
            }
        } catch (error) {
            console.error('❌ Error al cargar perfil:', error);
            Utils.showMessage('Error', 'No se pudo cargar el perfil', 'error');
        }
    }

    /**
     * Espera a que Bootstrap esté cargado
     */
    waitForBootstrap() {
        return new Promise((resolve) => {
            if (typeof bootstrap !== 'undefined') {
                resolve();
            } else {
                const checkBootstrap = setInterval(() => {
                    if (typeof bootstrap !== 'undefined') {
                        clearInterval(checkBootstrap);
                        resolve();
                    }
                }, 100);
            }
        });
    }

    /**
     * Carga los datos del usuario desde la sesión
     */
    loadUserData() {
        try {
            const user = AuthService.getUserSession();

            if (!user) {
                console.warn('No hay usuario en sesión');
                return;
            }

            // Actualizar foto de perfil
            const profilePic = document.getElementById('profilePicturePreview');
            if (profilePic) {
                profilePic.src = AuthService.getProfilePictureUrl();
            }

            // Actualizar nickname
            const nickDisplay = document.getElementById('userNickDisplay');
            if (nickDisplay) {
                nickDisplay.textContent = user.nick || 'Usuario';
            }

            // Actualizar email
            const emailDisplay = document.getElementById('userEmailDisplay');
            if (emailDisplay) {
                emailDisplay.textContent = user.email || '';
            }

            // Llenar formulario de datos personales
            const nameInput = document.getElementById('nameInput');
            if (nameInput) nameInput.value = user.name || '';

            const surname1Input = document.getElementById('surname1Input');
            if (surname1Input) surname1Input.value = user.surname1 || '';

            const surname2Input = document.getElementById('surname2Input');
            if (surname2Input) surname2Input.value = user.surname2 || '';

            const phoneInput = document.getElementById('phoneInput');
            if (phoneInput) phoneInput.value = user.phone || '';

            console.log('✅ Datos del usuario cargados');
        } catch (error) {
            console.error('❌ Error cargando datos del usuario:', error);
        }
    }

    /**
     * Adjunta eventos a los elementos del componente
     */
    attachEventListeners() {
        // Formulario de actualización de perfil
        const updateProfileForm = document.getElementById('updateProfileForm');
        if (updateProfileForm) {
            updateProfileForm.addEventListener('submit', (e) => this.handleUpdateProfile(e));
        }

        // Formulario de cambio de contraseña
        const updatePasswordForm = document.getElementById('updatePasswordForm');
        if (updatePasswordForm) {
            updatePasswordForm.addEventListener('submit', (e) => this.handleUpdatePassword(e));
        }

        // Input de foto
        const picInput = document.getElementById('picInput');
        if (picInput) {
            picInput.addEventListener('change', (e) => this.handleImageSelect(e));
        }

        // Formulario de foto
        const pictureForm = document.getElementById('pictureForm');
        if (pictureForm) {
            pictureForm.addEventListener('submit', (e) => this.handleUploadPicture(e));
        }

        // Modal de eliminación
        this.initDeleteAccountModal();

        // Logout
        const logoutLink = document.getElementById('logoutLink');
        if (logoutLink) {
            logoutLink.addEventListener('click', (e) => this.handleLogout(e));
        }
    }

    /**
     * Maneja la actualización del perfil
     */
    async handleUpdateProfile(e) {
        e.preventDefault();

        try {
            const name = document.getElementById('nameInput').value;
            const surname1 = document.getElementById('surname1Input').value;
            const surname2 = document.getElementById('surname2Input').value;
            const phone = document.getElementById('phoneInput').value;

            if (!name.trim() || !surname1.trim() || !phone.trim()) {
                this.showAlert('Por favor completa todos los campos requeridos', 'warning');
                return;
            }

            const requestBody = {
                name: name.trim(),
                surname1: surname1.trim(),
                surname2: surname2.trim(),
                phone: phone.trim()
            };

            console.log('📤 Enviando actualización de perfil a:', this.api);
            console.log('📋 Datos:', requestBody);

            const headers = {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            };

            const response = await fetch(this.api, {
                method: 'PUT',
                headers: headers,
                credentials: 'include',
                body: JSON.stringify(requestBody)
            });

            console.log('📨 Respuesta recibida. Status:', response.status, response.statusText);
            console.log('📨 Content-Type:', response.headers.get('content-type'));

            // Manejar errores de respuesta
            if (!response.ok) {
                let errorMessage = 'Error actualizando perfil';
                
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    try {
                        const error = await response.json();
                        errorMessage = error.message || error.error || errorMessage;
                    } catch (e) {
                        console.error('Error parseando respuesta JSON:', e);
                    }
                } else {
                    // Si no es JSON, intentar obtener el texto
                    try {
                        const text = await response.text();
                        console.error('Respuesta no-JSON del servidor:', text.substring(0, 200));
                        errorMessage = `Error del servidor (${response.status}): ${response.statusText}`;
                    } catch (e) {
                        errorMessage = `Error del servidor (${response.status}): ${response.statusText}`;
                    }
                }
                
                throw new Error(errorMessage);
            }

            const result = await response.json();

            console.log('✅ Perfil actualizado. Datos:', result);

            // Actualizar sesión con los nuevos datos
            if (result.data) {
                AuthService.setUserSession(result.data);
                this.loadUserData();
            }

            this.showAlert('Perfil actualizado correctamente', 'success');
            console.log('✅ Perfil actualizado exitosamente');
        } catch (error) {
            console.error('❌ Error actualizando perfil:', error);
            this.showAlert(error.message || 'Error al actualizar perfil', 'danger');
        }
    }

    /**
     * Maneja la selección de imagen
     */
    handleImageSelect(e) {
        const file = e.target.files[0];
        if (file) {
            // Mostrar preview
            const reader = new FileReader();
            reader.onload = (event) => {
                const preview = document.getElementById('profilePicturePreview');
                if (preview) {
                    preview.src = event.target.result;
                }
            };
            reader.readAsDataURL(file);

            // Mostrar botón de subir
            const submitBtn = document.getElementById('submitPicBtn');
            if (submitBtn) {
                submitBtn.classList.remove('d-none');
            }
        }
    }

    /**
     * Maneja la subida de la foto de perfil
     */
    async handleUploadPicture(e) {
        e.preventDefault();

        try {
            const picInput = document.getElementById('picInput');
            if (!picInput.files[0]) {
                this.showAlert('Por favor selecciona una imagen', 'warning');
                return;
            }

            const formData = new FormData();
            formData.append('profilePicture', picInput.files[0]);

            const loading = document.getElementById('pictureLoading');
            if (loading) loading.classList.remove('d-none');

            const response = await fetch(this.api + '/picture', {
                method: 'POST',
                credentials: 'include',
                body: formData
            });

            if (!response.ok) {
                let errorMessage = 'Error al subir imagen';
                
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    try {
                        const error = await response.json();
                        errorMessage = error.message || error.error || errorMessage;
                    } catch (e) {
                        console.error('Error parseando respuesta JSON:', e);
                    }
                } else {
                    try {
                        const text = await response.text();
                        console.error('Respuesta no-JSON del servidor:', text.substring(0, 200));
                        errorMessage = `Error del servidor (${response.status}): ${response.statusText}`;
                    } catch (e) {
                        errorMessage = `Error del servidor (${response.status}): ${response.statusText}`;
                    }
                }
                
                throw new Error(errorMessage);
            }

            const result = await response.json();

            // Actualizar sesión
            if (result.data) {
                AuthService.setUserSession(result.data);
                this.loadUserData();
            }

            // Limpiar form
            picInput.value = '';
            const submitBtn = document.getElementById('submitPicBtn');
            if (submitBtn) submitBtn.classList.add('d-none');

            this.showAlert('Foto de perfil actualizada correctamente', 'success');
            console.log('✅ Foto de perfil subida exitosamente');
        } catch (error) {
            console.error('❌ Error subiendo foto:', error);
            this.showAlert(error.message || 'Error al subir foto', 'danger');
        } finally {
            const loading = document.getElementById('pictureLoading');
            if (loading) loading.classList.add('d-none');
        }
    }

    /**
     * Maneja el cambio de contraseña
     */
    async handleUpdatePassword(e) {
        e.preventDefault();

        try {
            const currentPassword = document.getElementById('currentPasswordInput').value;
            const newPassword = document.getElementById('newPasswordInput').value;
            const confirmPassword = document.getElementById('confirmPasswordInput').value;

            if (!currentPassword || !newPassword || !confirmPassword) {
                this.showAlert('Todos los campos de contraseña son requeridos', 'warning');
                return;
            }

            if (newPassword !== confirmPassword) {
                this.showAlert('Las nuevas contraseñas no coinciden', 'danger');
                return;
            }

            if (newPassword.length < 6) {
                this.showAlert('La contraseña debe tener al menos 6 caracteres', 'warning');
                return;
            }

            const requestBody = {
                currentPassword: currentPassword,
                newPassword: newPassword,
                confirmPassword: confirmPassword
            };

            console.log('📤 Enviando cambio de contraseña a:', this.api + '/password');

            const response = await fetch(this.api + '/password', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify(requestBody)
            });

            console.log('📨 Respuesta recibida. Status:', response.status, response.statusText);

            if (!response.ok) {
                let errorMessage = 'Error al cambiar contraseña';
                
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    try {
                        const error = await response.json();
                        errorMessage = error.message || error.error || errorMessage;
                    } catch (e) {
                        console.error('Error parseando respuesta JSON:', e);
                    }
                } else {
                    try {
                        const text = await response.text();
                        console.error('Respuesta no-JSON del servidor:', text.substring(0, 200));
                        errorMessage = `Error del servidor (${response.status}): ${response.statusText}`;
                    } catch (e) {
                        errorMessage = `Error del servidor (${response.status}): ${response.statusText}`;
                    }
                }
                
                throw new Error(errorMessage);
            }

            // Limpiar formulario
            document.getElementById('updatePasswordForm').reset();

            this.showAlert('Contraseña actualizada correctamente', 'success');
            console.log('✅ Contraseña actualizada exitosamente');
        } catch (error) {
            console.error('❌ Error cambiando contraseña:', error);
            this.showAlert(error.message || 'Error al cambiar contraseña', 'danger');
        }
    }

    /**
     * Inicializa el modal de eliminación de cuenta
     */
    initDeleteAccountModal() {
        const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
        if (!confirmDeleteBtn) return;

        // Remover listeners previos
        const newBtn = confirmDeleteBtn.cloneNode(true);
        confirmDeleteBtn.parentNode.replaceChild(newBtn, confirmDeleteBtn);

        const deleteForm = document.getElementById('deleteForm');
        const delPassword = document.getElementById('delPassword');
        const deleteModal = document.getElementById('deleteModal');
        const confirmationModal = document.getElementById('confirmationModal');
        const emptyPasswordModal = document.getElementById('emptyPasswordModal');
        const credentialsErrorModal = document.getElementById('credentialsErrorModal');
        const confirmDeleteFinalBtn = document.getElementById('confirmDeleteFinalBtn');

        if (!deleteForm || !delPassword || !deleteModal) {
            console.error('❌ Faltan elementos del modal de eliminación');
            return;
        }

        // Agregar listener al botón de confirmación
        newBtn.addEventListener('click', async (e) => {
            e.preventDefault();

            const password = delPassword.value.trim();

            if (!password) {
                if (emptyPasswordModal) {
                    const modal = new bootstrap.Modal(emptyPasswordModal);
                    modal.show();
                }
                return;
            }

            // Validar contraseña con el backend antes de mostrar confirmación
            await this.validatePasswordBeforeDelete(password, deleteModal, confirmationModal, credentialsErrorModal, newBtn);
        });

        // Listener para confirmación final
        if (confirmDeleteFinalBtn) {
            confirmDeleteFinalBtn.addEventListener('click', async (e) => {
                e.preventDefault();
                await this.handleDeleteAccount();
            });
        }

        // Limpiar cuando se cierre el modal
        deleteModal.addEventListener('hidden.bs.modal', () => {
            if (delPassword) delPassword.value = '';
        });
    }

    /**
     * Valida la contraseña con el backend antes de mostrar el modal de confirmación final
     */
    async validatePasswordBeforeDelete(password, deleteModal, confirmationModal, credentialsErrorModal, confirmDeleteBtn) {
        try {
            // Deshabilitar botón temporalmente
            confirmDeleteBtn.disabled = true;
            confirmDeleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Validando...';

            console.log('📤 Validando contraseña...');

            // Hacer request para validar contraseña
            const response = await fetch(this.api + '/validate-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({ password: password })
            });

            if (response.ok) {
                // Contraseña correcta - cerrar primer modal y abrir confirmación
                console.log('✅ Contraseña validada correctamente');
                
                const deleteModalInstance = bootstrap.Modal.getInstance(deleteModal);
                if (deleteModalInstance) deleteModalInstance.hide();

                // Esperar a que se cierre para abrir la siguiente
                setTimeout(() => {
                    const confirmModalInstance = new bootstrap.Modal(confirmationModal);
                    confirmModalInstance.show();
                }, 300);
            } else {
                // Contraseña incorrecta - mostrar error modal
                console.log('❌ Contraseña incorrecta');
                
                if (credentialsErrorModal) {
                    const errorModalInstance = new bootstrap.Modal(credentialsErrorModal);
                    errorModalInstance.show();
                }
            }
        } catch (error) {
            console.error('❌ Error validando contraseña:', error);
            this.showAlert('Error al validar contraseña', 'danger');
        } finally {
            // Restaurar botón
            confirmDeleteBtn.disabled = false;
            confirmDeleteBtn.innerHTML = '<i class="fas fa-trash-alt me-2"></i>Sí, eliminar';
        }
    }

    /**
     * Maneja la eliminación de cuenta
     */
    async handleDeleteAccount() {
        try {
            const delPassword = document.getElementById('delPassword');
            const confirmDeleteFinalBtn = document.getElementById('confirmDeleteFinalBtn');
            const confirmationModal = document.getElementById('confirmationModal');

            if (!delPassword.value.trim()) {
                this.showAlert('La contraseña es requerida', 'warning');
                return;
            }

            // Deshabilitar botón
            if (confirmDeleteFinalBtn) {
                confirmDeleteFinalBtn.disabled = true;
                confirmDeleteFinalBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Eliminando...';
            }

            const response = await fetch(this.api + '/delete', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    password: delPassword.value.trim()
                })
            });

            if (!response.ok) {
                let errorMessage = 'Error al eliminar cuenta';
                
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    try {
                        const error = await response.json();
                        errorMessage = error.message || error.error || errorMessage;
                    } catch (e) {
                        console.error('Error parseando respuesta JSON:', e);
                    }
                } else {
                    try {
                        const text = await response.text();
                        console.error('Respuesta no-JSON del servidor:', text.substring(0, 200));
                        errorMessage = `Error del servidor (${response.status}): ${response.statusText}`;
                    } catch (e) {
                        errorMessage = `Error del servidor (${response.status}): ${response.statusText}`;
                    }
                }
                
                throw new Error(errorMessage);
            }

            const result = await response.json();

            if (response.ok && result.success) {
                // Cerrar modales
                if (confirmationModal) {
                    const modalInstance = bootstrap.Modal.getInstance(confirmationModal);
                    if (modalInstance) modalInstance.hide();
                }

                // Mostrar modal de despedida
                const goodbyeModal = document.getElementById('goodbyeModal');
                if (goodbyeModal) {
                    const modal = new bootstrap.Modal(goodbyeModal, { backdrop: 'static', keyboard: false });
                    modal.show();
                }

                // Limpiar sesión
                AuthService.logout();

                // Redirigir
                setTimeout(() => {
                    App.getInstance().navigateTo('/login');
                }, 2000);
            } else {
                // Error
                const credentialsErrorModal = document.getElementById('credentialsErrorModal');
                if (credentialsErrorModal) {
                    const modal = new bootstrap.Modal(credentialsErrorModal);
                    modal.show();
                }

                // Restaurar botón
                if (confirmDeleteFinalBtn) {
                    confirmDeleteFinalBtn.disabled = false;
                    confirmDeleteFinalBtn.innerHTML = '<i class="fas fa-trash-alt me-2"></i>Sí, eliminar permanentemente';
                }
            }
        } catch (error) {
            console.error('❌ Error eliminando cuenta:', error);

            const connectionErrorModal = document.getElementById('connectionErrorModal');
            if (connectionErrorModal) {
                const details = connectionErrorModal.querySelector('#connectionErrorDetails');
                if (details) details.textContent = error.message;
                const modal = new bootstrap.Modal(connectionErrorModal);
                modal.show();
            }

            // Restaurar botón
            const confirmDeleteFinalBtn = document.getElementById('confirmDeleteFinalBtn');
            if (confirmDeleteFinalBtn) {
                confirmDeleteFinalBtn.disabled = false;
                confirmDeleteFinalBtn.innerHTML = '<i class="fas fa-trash-alt me-2"></i>Sí, eliminar permanentemente';
            }
        }
    }

    /**
     * Muestra una alerta en pantalla
     */
    showAlert(message, type = 'success') {
        const alertId = type === 'success' ? 'successAlert' : 'errorAlert';
        const messageId = type === 'success' ? 'successMessage' : 'errorMessage';

        const alert = document.getElementById(alertId);
        const messageEl = document.getElementById(messageId);

        if (alert && messageEl) {
            messageEl.textContent = message;
            alert.classList.remove('d-none');

            // Auto-cerrar después de 5 segundos
            setTimeout(() => {
                alert.classList.add('d-none');
            }, 5000);
        }
    }

    /**
     * Maneja el logout del usuario
     */
    handleLogout(e) {
        e.preventDefault();

        AuthService.logout();
        console.log('✅ Sesión cerrada');

        App.getInstance().navigateTo('/login');
    }
}

// Registrar cuando el script esté listo
if (typeof AppScripts !== 'undefined') {
    AppScripts.register('profile');
}

// Exportar componente si estamos en módulo
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ProfileComponent;
}
