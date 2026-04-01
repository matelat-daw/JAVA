/**
 * navbar.component.js - Componente de la barra de navegación
 */

class NavbarComponent {
    constructor() {
        this.selector = '#navbar-container';
    }

    /**
     * Reinicializa el navbar (para cambios de autenticación)
     * Solo actualiza el estado sin recargar el HTML
     */
    async reinit() {
        try {
            // No recargar el HTML, solo actualizar el estado de autenticación
            this.updateAuthStatus();
            this.loadUserData();
        } catch (error) {
            console.error('Error al reinicializar navbar:', error);
        }
    }

    /**
     * Inicializa el componente
     */
    async init() {
        try {
            const response = await fetch('/frontend/src/app/components/navbar/navbar.component.html');
            const html = await response.text();
            
            const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = html;
                // Usar setTimeout para asegurar que AuthService esté cargado
                // 200ms es suficiente para que todos los scripts se ejecuten
                setTimeout(() => {
                    this.updateAuthStatus();
                    this.attachEventListeners();
                }, 200);
            }
        } catch (error) {
            console.error('Error al cargar navbar:', error);
        }
    }

    /**
     * Actualiza el estado de autenticación del navbar
     */
    updateAuthStatus() {
        // Validar que AuthService esté definido - SIN reintentos, solo una verificación
        if (typeof AuthService === 'undefined') {
            console.warn('⚠️ AuthService no está disponible. Mostrando vista por defecto (no autenticado)');
            this.showUnauthenticatedView();
            return;
        }

        const isAuthenticated = AuthService.isAuthenticated();
        
        // Mostrar/ocultar elementos según autenticación
        const guestLinks = document.getElementById('guestLinks');
        const registerLink = document.getElementById('registerLink');
        const loginLink = document.getElementById('loginLink');
        const authenticatedLinks = document.getElementById('authenticatedLinks');
        const usersLink = document.getElementById('usersLink');
        const userDropdown = document.getElementById('userDropdown');

        if (isAuthenticated) {
            // Usuario autenticado - mostrar panel de usuario
            if (guestLinks) guestLinks.style.display = 'none';
            if (registerLink) registerLink.style.display = 'none';
            if (loginLink) loginLink.style.display = 'none';
            if (authenticatedLinks) authenticatedLinks.style.display = 'block';
            if (usersLink) usersLink.style.display = 'block';
            if (userDropdown) userDropdown.style.display = 'block';

            // Cargar datos del usuario
            this.loadUserData();
        } else {
            // Usuario no autenticado - mostrar links de login/register
            if (guestLinks) guestLinks.style.display = 'block';
            if (registerLink) registerLink.style.display = 'block';
            if (loginLink) loginLink.style.display = 'block';
            if (authenticatedLinks) authenticatedLinks.style.display = 'none';
            if (usersLink) usersLink.style.display = 'none';
            if (userDropdown) userDropdown.style.display = 'none';
        }
    }

    /**
     * Muestra la vista de no autenticado (cuando AuthService no está disponible)
     */
    showUnauthenticatedView() {
        const guestLinks = document.getElementById('guestLinks');
        const registerLink = document.getElementById('registerLink');
        const loginLink = document.getElementById('loginLink');
        const authenticatedLinks = document.getElementById('authenticatedLinks');
        const usersLink = document.getElementById('usersLink');
        const userDropdown = document.getElementById('userDropdown');

        if (guestLinks) guestLinks.style.display = 'block';
        if (registerLink) registerLink.style.display = 'block';
        if (loginLink) loginLink.style.display = 'block';
        if (authenticatedLinks) authenticatedLinks.style.display = 'none';
        if (usersLink) usersLink.style.display = 'none';
        if (userDropdown) userDropdown.style.display = 'none';
    }

    /**
     * Carga los datos del usuario en el navbar
     */
    loadUserData() {
        // Validar que AuthService esté definido
        if (typeof AuthService === 'undefined') {
            return;
        }

        const user = AuthService.getUserSession();
        if (!user) return;

        // Actualizar foto de perfil
        const profilePic = document.getElementById('navbarProfilePic');
        if (profilePic) {
            profilePic.src = AuthService.getProfilePictureUrl();
        }

        // Actualizar nickname
        const userNick = document.getElementById('navbarUserNick');
        if (userNick) {
            userNick.textContent = user.nick || 'User';
        }

        // Actualizar nombre completo en dropdown
        const fullName = document.getElementById('dropdownFullName');
        if (fullName) {
            fullName.textContent = AuthService.getFullName();
        }
    }

    /**
     * Adjunta eventos a los elementos del navbar
     */
    attachEventListeners() {
        // Eventos de dropdown de usuario
        const logoutLink = document.getElementById('logoutLink');
        if (logoutLink) {
            logoutLink.addEventListener('click', (e) => this.handleLogout(e));
        }

        // Cerrar dropdown al hacer click en un enlace (en mobile)
        const dropdownItems = document.querySelectorAll('.dropdown-item');
        dropdownItems.forEach(item => {
            item.addEventListener('click', () => {
                const dropdown = document.querySelector('.navbar-collapse');
                if (dropdown && dropdown.classList.contains('show')) {
                    const toggler = document.querySelector('.navbar-toggler');
                    toggler.click();
                }
            });
        });

        // Adjuntar click handlers a los nav links
        const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
        navLinks.forEach(link => {
            link.addEventListener('click', () => {
                this.setActiveLink(link);
            });
        });
    }

    /**
     * Maneja el logout del usuario
     * @param {Event} e - Evento del click
     */
    handleLogout(e) {
        e.preventDefault();

        // Validar que AuthService esté definido
        if (typeof AuthService === 'undefined') {
            console.error('AuthService no está disponible');
            return;
        }

        if (confirm('¿Are you sure you want to logout?')) {
            AuthService.logout();
            
            // Limpiar instancias de componentes
            const app = App.getInstance();
            if (app && typeof app.clearComponentInstances === 'function') {
                app.clearComponentInstances();
            }
            
            Utils.showMessage(
                'Logged Out',
                'You have been successfully logged out.',
                'success'
            );

            // Limpiar navbar
            const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = '';
            }

            // Redirigir a home
            setTimeout(() => {
                App.getInstance().navigateTo('/');
            }, 300);
        }
    }

    /**
     * Establece el enlace activo en el navbar
     * @param {HTMLElement} link - Elemento de enlace
     */
    setActiveLink(link) {
        const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
        navLinks.forEach(l => l.classList.remove('active'));
        link.classList.add('active');
    }

    /**
     * Actualiza el enlace activo según la ruta actual
     * @param {string} route - Ruta actual
     */
    updateActiveLink(route) {
        const routeMap = {
            'home': '.nav-link-home',
            'register': '.nav-link-register',
            'login': '.nav-link-login',
            'dashboard': '.nav-link-dashboard',
            'users': '.nav-link-users'
        };

        const selector = routeMap[route] || '.nav-link-home';
        const link = document.querySelector(selector);
        if (link) {
            this.setActiveLink(link);
        }
    }
}

// Crear instancia global de manera segura
let NavBar = null;
try {
    NavBar = new NavbarComponent();
} catch (error) {
    console.error('Error al crear instancia de NavBar:', error);
    NavBar = null;
}

// Exponer globalmente para la verificación de carga
window.NavbarComponent = NavbarComponent;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') AppScripts.register('navbar');

