/**
 * app.js - Controlador principal de la aplicación
 * Maneja las rutas y la lógica central
 */

class App {
    constructor() {
        this.currentRoute = 'home';
        this.routes = {
            'home': this.loadHome,
            'register': this.loadRegister,
            'users': this.loadUsers,
            'login': this.loadLogin,
            'dashboard': this.loadDashboard,
            'profile': this.loadProfile
        };
        // Instancias de componentes
        this.loginComponent = null;
        this.dashboardComponent = null;
        this.registerComponent = null;
        this.profileComponent = null;
        this.usersComponent = null;
    }

    /**
     * Inicializa la aplicación
     */
    static init() {
        const instance = App.getInstance();
        instance.setupRouting();
        // Cargar navbar sin delay - ya debería estar disponible
        instance.loadNavbar();
    }

    /**
     * Obtiene la instancia singleton de App
     * @returns {App}
     */
    static getInstance() {
        if (!window._appInstance) {
            window._appInstance = new App();
        }
        return window._appInstance;
    }

    /**
     * Limpia todas las instancias de componentes
     * Se llama cuando el usuario hace logout
     */
    clearComponentInstances() {
        this.loginComponent = null;
        this.dashboardComponent = null;
        this.registerComponent = null;
        this.profileComponent = null;
        this.usersComponent = null;
        console.log('✅ Instancias de componentes limpias');
    }

    /**
     * Configura el sistema de rutas
     */
    setupRouting() {
        // Manejar cambios de ruta usando popstate
        window.addEventListener('popstate', () => this.handleRouteChange());
        
        // Interceptar clics en enlaces internos
        document.addEventListener('click', (e) => this.handleLinkClick(e));
        
        // Cargar ruta inicial
        this.handleRouteChange();
    }

    /**
     * Intercepta clics en enlaces internos
     */
    handleLinkClick(e) {
        const link = e.target.closest('a');
        if (!link) return;
        
        const href = link.getAttribute('href');
        if (!href || href.startsWith('http') || href.startsWith('#')) return;
        
        e.preventDefault();
        this.navigateTo(href);
    }

    /**
     * Navega a una ruta
     */
    navigateTo(path) {
        let fullPath = path;
        // Las rutas ahora son relativas a la raíz /
        if (window.location.pathname !== fullPath) {
            window.history.pushState(null, '', fullPath);
        }
        this.handleRouteChange();
    }

    /**
     * Maneja los cambios de ruta
     */
    handleRouteChange() {
        const path = window.location.pathname;
        // Extraer la ruta (ahora la raíz es /)
        const parts = path.split('/').filter(Boolean);
        const route = parts.length > 0 ? parts[0] : 'home';
        const param = parts.length > 1 ? parts[1] : null;
        
        console.log(`📍 Navegando a: ${route}${param ? '/' + param : ''} (ruta completa: ${path})`);
        
        this.currentRoute = route;
        
        if (this.routes[route]) {
            this.routes[route].call(this, param);
        } else {
            this.loadHome();
        }

        // Actualizar navbar activo y reinicializar su estado de autenticación
        if (typeof NavBar !== 'undefined' && NavBar && typeof NavBar.updateActiveLink === 'function') {
            NavBar.updateActiveLink(route);
        }
        
        // Reinitializar navbar para reflejar cambios de autenticación después de que se cargue el componente
        if (typeof NavBar !== 'undefined' && NavBar && typeof NavBar.reinit === 'function') {
            setTimeout(() => {
                NavBar.reinit();
            }, 50);
        }
    }

    /**
     * Carga el navbar
     */
    async loadNavbar() {
        // Crear NavBar si no existe
        if (!NavBar) {
            if (typeof NavbarComponent !== 'undefined') {
                try {
                    NavBar = new NavbarComponent();
                } catch (error) {
                    console.error('Error al crear NavBar:', error);
                    return;
                }
            } else {
                console.error('NavbarComponent no está definido');
                return;
            }
        }
        
        // Verificar que NavBar tenga el método init
        if (typeof NavBar.init === 'function') {
            await NavBar.init();
        }
    }

    /**
     * Carga la vista de inicio
     */
    loadHome() {
        const outlet = document.getElementById('router-outlet');
        if (outlet) {
            const isAuthenticated = typeof AuthService !== 'undefined' && AuthService.isAuthenticated();
            
            if (isAuthenticated) {
                // Si el usuario est� autenticado, mostrar bienvenida sin botones
                outlet.innerHTML = `
                    <div class="container mt-5">
                        <div class="row justify-content-center">
                            <div class="col-md-8">
                                <div class="card shadow">
                                    <div class="card-body text-center py-5">
                                        <i class="fas fa-users display-1 text-primary mb-3"></i>
                                        <h1 class="card-title mb-3">Welcome to Clients API</h1>
                                        <p class="lead text-muted mb-4">
                                            Manage your users easily and efficiently
                                        </p>
                                        <p class="text-muted">
                                            Use the navigation menu to access the dashboard or manage your account.
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                `;
            } else {
                // Si el usuario NO est� autenticado, mostrar con botones de login/register
                outlet.innerHTML = `
                    <div class="container mt-5">
                        <div class="row justify-content-center">
                            <div class="col-md-8">
                                <div class="card shadow">
                                    <div class="card-body text-center py-5">
                                        <i class="fas fa-users display-1 text-primary mb-3"></i>
                                        <h1 class="card-title mb-3">Welcome to Clients API</h1>
                                        <p class="lead text-muted mb-4">
                                            Manage your users easily and efficiently
                                        </p>
                                        <div class="d-flex justify-content-center gap-3">
                                            <a href="/register" class="btn btn-primary btn-lg d-flex align-items-center">
                                                <i class="fas fa-user-plus me-2"></i> <span>Register</span>
                                            </a>
                                            <a href="/login" class="btn btn-outline-primary btn-lg d-flex align-items-center">
                                                <i class="fas fa-sign-in-alt me-2"></i> <span>Sign In</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                `;
            }
        }
    }

    /**
     * Carga el formulario de registro
     */
    loadRegister() {
        console.log('📝 Cargando página de registro...');
        // Mostrar HTML de registro inline mientras se carga
        const outlet = document.getElementById('router-outlet');
        if (outlet) {
            outlet.innerHTML = `
                <div class="register-container">
                    <div class="text-center">
                        <div class="spinner-border text-primary" role="status" style="margin-top: 50px;">
                            <span class="visually-hidden">Cargando...</span>
                        </div>
                        <p class="mt-3">Cargando formulario de registro...</p>
                    </div>
                </div>
            `;
        }

        // Crear instancia bajo demanda si no existe
        if (!this.registerComponent) {
            // Verificar si RegisterComponent está disponible
            if (typeof RegisterComponent === 'undefined') {
                console.warn('⚠️ RegisterComponent no está disponible. Verificando disponibilidad...');
                setTimeout(() => {
                    console.log('Verificando de nuevo RegisterComponent...');
                    if (typeof RegisterComponent !== 'undefined') {
                        console.log('✅ RegisterComponent ahora está disponible');
                        this.registerComponent = new RegisterComponent();
                        this.registerComponent.init();
                    } else {
                        console.error('❌ RegisterComponent no está disponible');
                        outlet.innerHTML = `
                            <div class="alert alert-danger m-5">
                                <h4>Error al cargar el formulario</h4>
                                <p>No se pudo cargar el componente de registro. Por favor, recarga la página.</p>
                                <button class="btn btn-primary" onclick="window.location.reload()">Recargar página</button>
                            </div>
                        `;
                    }
                }, 100);
                return;
            }
            this.registerComponent = new RegisterComponent();
        }
        this.registerComponent.init();
    }

    /**
     * Carga el formulario de login
     */
    loadLogin() {
        console.log('🔐 Cargando página de login...');
        // Mostrar HTML de login inline mientras se carga
        const outlet = document.getElementById('router-outlet');
        if (outlet) {
            outlet.innerHTML = `
                <div class="login-container">
                    <div class="text-center">
                        <div class="spinner-border text-primary" role="status" style="margin-top: 50px;">
                            <span class="visually-hidden">Cargando...</span>
                        </div>
                        <p class="mt-3">Cargando formulario de login...</p>
                    </div>
                </div>
            `;
        }

        // Crear instancia bajo demanda si no existe
        if (!this.loginComponent) {
            // Verificar si LoginComponent está disponible
            if (typeof LoginComponent === 'undefined') {
                console.warn('⚠️ LoginComponent no está disponible. Esperando...');
                // Intentar cargar después de un delay
                setTimeout(() => {
                    console.log('Verificando de nuevo LoginComponent...');
                    if (typeof LoginComponent !== 'undefined') {
                        console.log('✅ LoginComponent ahora está disponible');
                        this.loginComponent = new LoginComponent();
                        this.loginComponent.init();
                    } else {
                        console.error('❌ LoginComponent sigue sin estar disponible, usando fallback');
                        this.loadLoginFallback();
                    }
                }, 100);
                return;
            }
            this.loginComponent = new LoginComponent();
        }
        this.loginComponent.init();
    }

    /**
     * Formulario de login fallback si el componente no carga
     */
    loadLoginFallback() {
        const outlet = document.getElementById('router-outlet');
        if (outlet) {
            outlet.innerHTML = `
                <div class="container mt-5">
                    <div class="row justify-content-center">
                        <div class="col-md-6">
                            <div class="card shadow-lg">
                                <div class="card-header bg-primary text-white">
                                    <h4 class="mb-0">
                                        <i class="fas fa-sign-in-alt"></i> Sign In
                                    </h4>
                                </div>
                                <div class="card-body">
                                    <form id="loginForm">
                                        <div class="mb-3">
                                            <label class="form-label"><i class="fas fa-envelope"></i> Email</label>
                                            <input type="email" class="form-control" name="email" placeholder="your.email@example.com" required>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label"><i class="fas fa-lock"></i> Password</label>
                                            <input type="password" class="form-control" name="password" placeholder="Enter your password" required>
                                        </div>
                                        <button type="submit" class="btn btn-primary w-100">Sign In</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;

            // Adjuntar evento al formulario
            const form = document.getElementById('loginForm');
            if (form) {
                form.addEventListener('submit', async (e) => {
                    e.preventDefault();
                    
                    const email = form.querySelector('input[name="email"]').value;
                    const password = form.querySelector('input[name="password"]').value;

                    // Verificar que AuthService esté disponible
                    if (typeof AuthService === 'undefined') {
                        console.error('AuthService no disponible');
                        if (typeof Utils !== 'undefined' && typeof Utils.showMessage === 'function') {
                            Utils.showMessage('Error', 'Sistema no disponible. Por favor, recarga la página.', 'error');
                        } else {
                            alert('Sistema no disponible. Por favor, recarga la página.');
                        }
                        return;
                    }

                    try {
                        // Mostrar estado de carga
                        const btn = form.querySelector('button[type="submit"]');
                        const originalText = btn.innerHTML;
                        btn.disabled = true;
                        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Signing in...';

                        const result = await AuthService.login(email, password);
                        
                        if (result.success) {
                            // Mostrar éxito
                            if (typeof Utils !== 'undefined' && typeof Utils.showMessage === 'function') {
                                Utils.showMessage('Success', '¡Bienvenido!', 'success');
                            }
                            // Redirigir al dashboard
                            setTimeout(() => {
                                this.navigateTo('/dashboard');
                            }, 1500);
                        } else {
                            // Error del servidor
                            if (typeof Utils !== 'undefined' && typeof Utils.showMessage === 'function') {
                                Utils.showMessage('Error', result.message || 'Invalid credentials', 'error');
                            } else {
                                alert(result.message || 'Invalid credentials');
                            }
                            btn.disabled = false;
                            btn.innerHTML = originalText;
                        }
                    } catch (error) {
                        console.error('Login error:', error);
                        if (typeof Utils !== 'undefined' && typeof Utils.showMessage === 'function') {
                            Utils.showMessage('Error', 'Error during login: ' + error.message, 'error');
                        } else {
                            alert('Error during login: ' + error.message);
                        }
                        const btn = form.querySelector('button[type="submit"]');
                        btn.disabled = false;
                        btn.innerHTML = 'Sign In';
                    }
                });
            }
        }
    }

    /**
     * Carga el dashboard/bienvenida del usuario autenticado
     */
    loadDashboard() {
        console.log('📊 Cargando dashboard...');
        // Mostrar HTML de dashboard inline mientras se carga
        const outlet = document.getElementById('router-outlet');
        if (outlet) {
            outlet.innerHTML = `
                <div class="dashboard-container">
                    <div class="text-center">
                        <div class="spinner-border text-primary" role="status" style="margin-top: 50px;">
                            <span class="visually-hidden">Cargando...</span>
                        </div>
                        <p class="mt-3">Cargando tu dashboard...</p>
                    </div>
                </div>
            `;
        }

        // Crear instancia bajo demanda si no existe
        if (!this.dashboardComponent) {
            // Verificar si DashboardComponent está disponible
            if (typeof DashboardComponent === 'undefined') {
                console.warn('⚠️ DashboardComponent no está disponible. Verificando disponibilidad...');
                setTimeout(() => {
                    console.log('Verificando de nuevo DashboardComponent...');
                    if (typeof DashboardComponent !== 'undefined') {
                        console.log('✅ DashboardComponent ahora está disponible');
                        this.dashboardComponent = new DashboardComponent();
                        this.dashboardComponent.init();
                    } else {
                        console.error('❌ DashboardComponent no está disponible');
                        outlet.innerHTML = `
                            <div class="alert alert-danger m-5">
                                <h4>Error al cargar el dashboard</h4>
                                <p>No se pudo cargar el componente de dashboard. Por favor, recarga la página.</p>
                                <button class="btn btn-primary" onclick="window.location.reload()">Recargar página</button>
                            </div>
                        `;
                    }
                }, 100);
                return;
            }
            this.dashboardComponent = new DashboardComponent();
        }
        this.dashboardComponent.init();
    }

    /**     * Carga la página de perfil del usuario
     */
    loadProfile() {
        console.log('👤 Cargando página de perfil...');
        
        // Verificar si el usuario está autenticado
        if (!AuthService.isAuthenticated()) {
            console.warn('Usuario no autenticado. Redirigiendo a login...');
            this.navigateTo('/login');
            return;
        }

        // Mostrar HTML de perfil inline mientras se carga
        const outlet = document.getElementById('router-outlet');
        if (outlet) {
            outlet.innerHTML = `
                <div class="profile-container">
                    <div class="text-center">
                        <div class="spinner-border text-primary" role="status" style="margin-top: 50px;">
                            <span class="visually-hidden">Cargando perfil...</span>
                        </div>
                        <p class="mt-3">Cargando tu perfil...</p>
                    </div>
                </div>
            `;
        }

        // Crear instancia bajo demanda si no existe
        if (!this.profileComponent) {
            // Verificar si ProfileComponent está disponible
            if (typeof ProfileComponent === 'undefined') {
                console.warn('⚠️ ProfileComponent no está disponible. Verificando disponibilidad...');
                setTimeout(() => {
                    console.log('Verificando de nuevo ProfileComponent...');
                    if (typeof ProfileComponent !== 'undefined') {
                        console.log('✅ ProfileComponent ahora está disponible');
                        this.profileComponent = new ProfileComponent();
                        this.profileComponent.init();
                    } else {
                        console.error('❌ ProfileComponent no está disponible');
                        outlet.innerHTML = `
                            <div class="alert alert-danger m-5">
                                <h4>Error al cargar el perfil</h4>
                                <p>No se pudo cargar el componente de perfil. Por favor, recarga la página.</p>
                                <button class="btn btn-primary" onclick="window.location.reload()">Recargar página</button>
                            </div>
                        `;
                    }
                }, 100);
                return;
            }
            this.profileComponent = new ProfileComponent();
        }
        this.profileComponent.init();
    }

    /**     * Carga la lista de usuarios (placeholder)
     */
    loadUsers(userId) {
        console.log('👥 Cargando página de usuarios...', userId ? '(ID: ' + userId + ')' : '(lista)');
        
        // Si hay un userId, cargar detalles
        if (userId) {
            return this.loadUserDetails(userId);
        }
        
        // Mostrar HTML de usuarios inline mientras se carga
        const outlet = document.getElementById('router-outlet');
        if (outlet) {
            outlet.innerHTML = `
                <div class="users-container">
                    <div class="text-center">
                        <div class="spinner-border text-primary" role="status" style="margin-top: 50px;">
                            <span class="visually-hidden">Cargando usuarios...</span>
                        </div>
                        <p class="mt-3">Cargando lista de usuarios...</p>
                    </div>
                </div>
            `;
        }

        // Crear instancia bajo demanda si no existe
        if (!this.usersComponent) {
            // Verificar si UsersComponent está disponible
            if (typeof UsersComponent === 'undefined') {
                console.warn('⚠️ UsersComponent no está disponible. Verificando disponibilidad...');
                setTimeout(() => {
                    console.log('Verificando de nuevo UsersComponent...');
                    if (typeof UsersComponent !== 'undefined') {
                        console.log('✅ UsersComponent ahora está disponible');
                        this.usersComponent = new UsersComponent();
                        window.UsersComponentInstance = this.usersComponent;
                        this.usersComponent.init();
                    } else {
                        console.error('❌ UsersComponent no está disponible');
                        outlet.innerHTML = `
                            <div class="alert alert-danger m-5">
                                <h4>Error al cargar la lista de usuarios</h4>
                                <p>No se pudo cargar el componente de usuarios. Por favor, recarga la página.</p>
                                <button class="btn btn-primary" onclick="window.location.reload()">Recargar página</button>
                            </div>
                        `;
                    }
                }, 100);
                return;
            }
            this.usersComponent = new UsersComponent();
            window.UsersComponentInstance = this.usersComponent;
        }
        this.usersComponent.init();
    }

    /**
     * Carga los detalles de un usuario específico
     */
    loadUserDetails(userId) {
        console.log('[UserDetails] Cargando detalles del usuario:', userId);
        
        // DEBUGGING: Verificar qué está disponible en window
        console.log('[DEBUG] Verificando disponibilidad de componentes:');
        console.log('  - UserDetailsComponent:', typeof window.UserDetailsComponent);
        console.log('  - AuthService:', typeof window.AuthService);
        console.log('  - UserService:', typeof window.UserService);
        
        // Listar todas los objetos en AppScripts si existen
        if (typeof AppScripts !== 'undefined') {
            console.log('  - AppScripts.loaded:', Array.from(AppScripts.loaded));
        }

        try {
            if (typeof UserDetailsComponent === 'undefined') {
                console.error('[ERROR] UserDetailsComponent NO esta definida');
                console.error('[ERROR] window.UserDetailsComponent:', window.UserDetailsComponent);
                throw new Error('UserDetailsComponent no esta disponible. Intenta recargar la pagina (Ctrl+Shift+R)');
            }
            
            console.log('[SUCCESS] UserDetailsComponent esta disponible, creando instancia...');
            const component = new UserDetailsComponent();
            console.log('[SUCCESS] Instancia creada, inicializando con userId:', userId);
            component.init(userId);
        } catch (error) {
            console.error('[ERROR]', error);
            const outlet = document.getElementById('router-outlet');
            if (outlet) {
                outlet.innerHTML = '<div class="alert alert-danger m-5"><h5>Error</h5><p>' + error.message + '</p><a href="/users" class="btn btn-primary">Volver</a></div>';
            }
        }
    }
}

// Exponer globalmente para la verificación de carga
window.App = App;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') AppScripts.register('app');


