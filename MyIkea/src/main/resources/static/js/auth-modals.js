// Auth Modals Handler
// Maneja los modales de login, logout y errores de registro
// Se ejecuta cada vez que se carga la página de auth (compatible con SPA)

// Usar window para evitar SyntaxError al redeclarar let
if (!window.authModalsInitialized) {
    window.authModalsInitialized = false;
}

function initAuthModals() {
    // Prevenir múltiples inicializaciones simultáneas
    if (window.authModalsInitialized) {
        console.log('[AuthModals] Ya inicializado, ignorando');
        return;
    }
    window.authModalsInitialized = true;
    
    console.log('[AuthModals] Inicializando modales de autenticación');
    
    // Mostrar modal de error de autenticación
    const authErrorModal = document.getElementById('authErrorModal');
    if (authErrorModal) {
        console.log('[AuthModals] ✓ Encontrado authErrorModal');
        // Usar setTimeout para asegurar que Bootstrap esté cargado
        setTimeout(() => {
            try {
                const modal = new bootstrap.Modal(authErrorModal);
                modal.show();
                console.log('[AuthModals] ✓ Error modal mostrado');
            } catch (e) {
                console.error('[AuthModals] ✗ Error al mostrar authErrorModal:', e);
            }
        }, 100);
    }

    // Mostrar modal de logout exitoso
    const logoutSuccessModal = document.getElementById('logoutSuccessModal');
    if (logoutSuccessModal) {
        console.log('[AuthModals] ✓ Encontrado logoutSuccessModal');
        setTimeout(() => {
            try {
                const modal = new bootstrap.Modal(logoutSuccessModal);
                modal.show();
                console.log('[AuthModals] ✓ Logout success modal mostrado');
            } catch (e) {
                console.error('[AuthModals] ✗ Error al mostrar logoutSuccessModal:', e);
            }
        }, 100);
    }

    // Mostrar modal de contraseñas no coinciden
    const passwordMismatchModal = document.getElementById('passwordMismatchModal');
    if (passwordMismatchModal) {
        console.log('[AuthModals] ✓ Encontrado passwordMismatchModal');
        setTimeout(() => {
            try {
                const modal = new bootstrap.Modal(passwordMismatchModal);
                modal.show();
                console.log('[AuthModals] ✓ Password mismatch modal mostrado');
            } catch (e) {
                console.error('[AuthModals] ✗ Error al mostrar passwordMismatchModal:', e);
            }
        }, 100);
    }

    // Mostrar modal de contraseñas no coinciden en registro
    const passwordMismatchRegisterModal = document.getElementById('passwordMismatchRegisterModal');
    if (passwordMismatchRegisterModal) {
        console.log('[AuthModals] ✓ Encontrado passwordMismatchRegisterModal');
        setTimeout(() => {
            try {
                const modal = new bootstrap.Modal(passwordMismatchRegisterModal);
                modal.show();
                console.log('[AuthModals] ✓ Password mismatch register modal mostrado');
            } catch (e) {
                console.error('[AuthModals] ✗ Error al mostrar passwordMismatchRegisterModal:', e);
            }
        }, 100);
    }

    // Mostrar modal de registro exitoso
    const registrationSuccessModal = document.getElementById('registrationSuccessModal');
    if (registrationSuccessModal) {
        console.log('[AuthModals] ✓ Encontrado registrationSuccessModal');
        setTimeout(() => {
            try {
                const modal = new bootstrap.Modal(registrationSuccessModal, { backdrop: 'static', keyboard: false });
                modal.show();
                console.log('[AuthModals] ✓ Registration success modal mostrado');
            } catch (e) {
                console.error('[AuthModals] ✗ Error al mostrar registrationSuccessModal:', e);
            }
        }, 100);
    }

    console.log('[AuthModals] ✓ Inicialización completada');
}

// Hacer la función global para que otros scripts puedan llamarla
window.initAuthModals = initAuthModals;

// EJECUCIÓN INMEDIATA: Se ejecuta cuando el script se carga, sin esperar eventos
console.log('[AuthModals] Script cargado, ejecutando initAuthModals...');
initAuthModals();

// Ejecutar también en DOMContentLoaded como fallback (solo si aún no se ha inicializado)
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        if (!window.authModalsInitialized) {
            console.log('[AuthModals] DOMContentLoaded - ejecutando fallback');
            initAuthModals();
        }
    });
}
