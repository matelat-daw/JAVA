/**
 * auth.service.js - Servicio de autenticación
 */

class AuthService {
    /**
     * Información del usuario autenticado en sesión
     */
    static userSession = null;
    
    /**
     * Token JWT del usuario
     */
    static jwtToken = null;

    /**
     * Login del usuario
     * @param {string} email - Email del usuario
     * @param {string} password - Contraseña del usuario
     * @returns {Promise<Object>}
     */
    static async login(email, password) {
        try {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.LOGIN;
            const data = {
                email: email,
                password: password
            };

            console.log('🔐 Intentando login con:', { email });

            const response = await Utils.makeRequest('POST', url, data);

            // Si la respuesta es exitosa, guardar datos del usuario
            if (response.success || (response.code === 200 && response.data)) {
                const userData = response.data || response.user;
                const token = response.token;
                
                // Guardar datos del usuario en sessionStorage
                this.setUserSession(userData);
                
                // Guardar token JWT si viene en la respuesta
                if (token) {
                    this.setJwtToken(token);
                }

                console.log('✅ Login exitoso, usuario:', userData);
                console.log('🔑 Token JWT guardado:', !!token);
                return {
                    success: true,
                    message: response.message || 'Login exitoso',
                    user: userData,
                    token: token
                };
            }

            return {
                success: false,
                message: response.message || 'Error en login'
            };
        } catch (error) {
            console.error('❌ Error en login:', error);
            throw error;
        }
    }

    /**
     * Guarda los datos del usuario en sesión
     * @param {Object} userData - Datos del usuario
     */
    static setUserSession(userData) {
        this.userSession = userData;
        sessionStorage.setItem('user_session', JSON.stringify(userData));
    }

    /**
     * Obtiene los datos del usuario de sesión
     * @returns {Object|null}
     */
    static getUserSession() {
        if (!this.userSession) {
            const stored = sessionStorage.getItem('user_session');
            if (stored) {
                this.userSession = JSON.parse(stored);
            }
        }
        return this.userSession;
    }

    /**
     * Verifica si el usuario está autenticado
     * @returns {boolean}
     */
    static isAuthenticated() {
        const user = this.getUserSession();
        return user !== null && user !== undefined;
    }

    /**
     * Cierra la sesión del usuario (logout)
     */
    static logout() {
        this.userSession = null;
        sessionStorage.removeItem('user_session');
        console.log('👋 Sesión cerrada');
    }

    /**
     * Obtiene el nombre completo del usuario
     * @returns {string}
     */
    static getFullName() {
        const user = this.getUserSession();
        if (!user) return '';
        
        const name = user.name || '';
        const surname1 = user.surname1 || '';
        const surname2 = user.surname2 || '';
        
        return `${name} ${surname1} ${surname2}`.trim();
    }

    /**
     * Obtiene la URL de la foto de perfil del usuario
     * @returns {string}
     */
    static getProfilePictureUrl() {
        const user = this.getUserSession();
        if (!user || !user.profileImg) {
            return 'https://via.placeholder.com/150?text=No+Photo';
        }
        
        let imgPath = user.profileImg;
        
        // Limpiar ruta: remover "images/" si existe (para compatibilidad con datos antiguos)
        if (imgPath.startsWith('images/')) {
            imgPath = imgPath.replace('images/', '');
        }
        
        // Construir URL completa desde la API
        // URLs válidas: /api/images/ID/profile.jpg o /api/images/default/male.png
        return `${API_CONFIG.BASE_URL}/images/${imgPath}`;
    }

    /**
     * Obtiene el nickname del usuario
     * @returns {string}
     */
    static getNick() {
        const user = this.getUserSession();
        return user ? user.nick : '';
    }

    /**
     * Obtiene el email del usuario
     * @returns {string}
     */
    static getEmail() {
        const user = this.getUserSession();
        return user ? user.email : '';
    }

    /**
     * Obtiene el rol del usuario
     * @returns {string}
     */
    static getRole() {
        const user = this.getUserSession();
        return user ? user.role : 'USER';
    }

    /**
     * Guarda el token JWT
     * @param {string} token - Token JWT
     */
    static setJwtToken(token) {
        this.jwtToken = token;
        if (token) {
            sessionStorage.setItem('jwt_token', token);
            console.log('🔑 Token JWT guardado en sessionStorage');
        }
    }

    /**
     * Obtiene el token JWT
     * @returns {string|null}
     */
    static getJwtToken() {
        if (!this.jwtToken) {
            const stored = sessionStorage.getItem('jwt_token');
            if (stored) {
                this.jwtToken = stored;
            }
        }
        return this.jwtToken;
    }

    /**
     * Obtiene el header Authorization con el token JWT
     * @returns {Object}
     */
    static getAuthorizationHeader() {
        const token = this.getJwtToken();
        if (token) {
            return {
                'Authorization': `Bearer ${token}`
            };
        }
        return {};
    }

    /**
     * Cierra la sesión del usuario (logout) - Actualizado para limpiar también el JWT
     */
    static logoutWithJwt() {
        this.userSession = null;
        this.jwtToken = null;
        sessionStorage.removeItem('user_session');
        sessionStorage.removeItem('jwt_token');
        console.log('👋 Sesión cerrada y token JWT eliminado');
    }
}

// Exponer globalmente para la verificación de carga
window.AuthService = AuthService;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') AppScripts.register('auth.service');
