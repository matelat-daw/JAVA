/**
 * dashboard.component.js - Componente de Dashboard/Welcome
 */

class DashboardComponent {
    constructor() {
        this.selector = '#router-outlet';
    }

    /**
     * Inicializa el componente
     */
    async init() {
        try {
            // Verificar si el usuario está autenticado
            if (!AuthService.isAuthenticated()) {
                // Redirigir al login si no está autenticado
                App.getInstance().navigateTo('/login');
                return;
            }

            const response = await fetch('/frontend/src/app/components/dashboard/dashboard.component.html');
            const html = await response.text();
            
            const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = html;
                
                // Cargar datos del usuario
                this.loadUserData();
                
                // Adjuntar eventos
                this.attachEventListeners();
            }
        } catch (error) {
            console.error('Error al cargar dashboard:', error);
            Utils.showMessage('Error', 'No se pudo cargar el dashboard', 'error');
        }
    }

    /**
     * Carga los datos del usuario en la página
     */
    loadUserData() {
        const user = AuthService.getUserSession();
        
        if (!user) return;

        // Actualizar nombre en bienvenida
        const fullName = AuthService.getFullName();
        const userNameDisplay = document.getElementById('userNameDisplay');
        if (userNameDisplay) {
            userNameDisplay.innerHTML = `<strong>${user.nick}</strong> / ${fullName}`;
        }

        // Actualizar foto de perfil
        const profilePictureImg = document.getElementById('profilePictureImg');
        if (profilePictureImg) {
            const photoUrl = AuthService.getProfilePictureUrl();
            profilePictureImg.src = photoUrl;
        }

        // Actualizar información del usuario
        const nickDisplay = document.getElementById('nickDisplay');
        if (nickDisplay) {
            nickDisplay.textContent = user.nick || 'N/A';
        }

        const emailDisplay = document.getElementById('emailDisplay');
        if (emailDisplay) {
            emailDisplay.textContent = user.email || 'N/A';
        }

        const fullNameDisplay = document.getElementById('fullNameDisplay');
        if (fullNameDisplay) {
            fullNameDisplay.textContent = fullName || 'N/A';
        }

        const roleDisplay = document.getElementById('roleDisplay');
        if (roleDisplay) {
            const role = AuthService.getRole();
            roleDisplay.textContent = role;
            
            // Cambiar color según rol
            roleDisplay.className = 'badge';
            if (role === 'ADMIN') {
                roleDisplay.classList.add('bg-danger');
            } else if (role === 'MODERATOR' || role === 'PREMIUM') {
                roleDisplay.classList.add('bg-warning');
            } else {
                roleDisplay.classList.add('bg-info');
            }
        }
    }

    /**
     * Adjunta eventos a los elementos de la página
     */
    attachEventListeners() {
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => this.handleLogout(e));
        }
    }

    /**
     * Maneja el logout del usuario
     * @param {Event} e - Evento del click
     */
    handleLogout(e) {
        e.preventDefault();

        // Mostrar confirmación
        if (confirm('¿Are you sure you want to logout?')) {
            // Cerrar sesión
            AuthService.logout();

            // Limpiar instancias de componentes
            const app = App.getInstance();
            if (app && typeof app.clearComponentInstances === 'function') {
                app.clearComponentInstances();
            }

            // Mostrar mensaje
            Utils.showMessage(
                'Logged Out',
                'You have been successfully logged out.',
                'success'
            );

            // Actualizar navbar antes de navegar
            setTimeout(() => {
                // Reinicializar navbar para mostrar estado de no autenticado
                if (NavBar && typeof NavBar.reinit === 'function') {
                    NavBar.reinit();
                }
                // Redirigir a home después de reinicializar
                setTimeout(() => {
                    App.getInstance().navigateTo('/');
                }, 300);
            }, 1000);
        }
    }
}

// Exponer globalmente para la verificación de carga
window.DashboardComponent = DashboardComponent;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') AppScripts.register('dashboard');
