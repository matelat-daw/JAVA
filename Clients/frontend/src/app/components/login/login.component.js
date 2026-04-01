/**
 * login.component.js - Componente de Login
 */

class LoginComponent {
    constructor() {
        this.selector = '#router-outlet';
        this.formId = 'loginForm';
    }

    /**
     * Inicializa el componente
     */
    async init() {
        try {
            // Verificar si el usuario ya está autenticado
            if (AuthService.isAuthenticated()) {
                console.log('✅ Usuario ya autenticado, redirigiendo al dashboard...');
                App.getInstance().navigateTo('/dashboard');
                return;
            }

            const response = await fetch('/frontend/src/app/components/login/login.component.html');
            const html = await response.text();
            
            const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = html;
                
                // Adjuntar eventos
                this.attachEventListeners();
            }
        } catch (error) {
            console.error('Error al cargar formulario de login:', error);
            Utils.showMessage('Error', 'No se pudo cargar el formulario de login', 'error');
        }
    }

    /**
     * Adjunta eventos a los elementos del formulario
     */
    attachEventListeners() {
        const form = document.getElementById(this.formId);
        if (!form) return;

        // Evento de envío del formulario
        form.addEventListener('submit', (e) => this.handleSubmit(e));

        // Validación en tiempo real
        const inputs = form.querySelectorAll('input[type="email"], input[type="password"]');
        inputs.forEach(input => {
            input.addEventListener('blur', () => this.validateField(input));
            input.addEventListener('input', () => {
                if (input.classList.contains('is-invalid')) {
                    this.validateField(input);
                }
            });
        });
    }

    /**
     * Valida un campo individual
     * @param {HTMLElement} input - Campo a validar
     */
    validateField(input) {
        const name = input.name;
        const value = input.value.trim();
        let isValid = true;
        let errorMessage = '';

        switch(name) {
            case 'email':
                if (!value) {
                    isValid = false;
                    errorMessage = 'Email is required';
                } else if (!VALIDATION_PATTERNS.EMAIL.test(value)) {
                    isValid = false;
                    errorMessage = 'Please enter a valid email address';
                }
                break;

            case 'password':
                if (!value) {
                    isValid = false;
                    errorMessage = 'Password is required';
                } else if (value.length < 6) {
                    isValid = false;
                    errorMessage = 'Password must be at least 6 characters';
                }
                break;
        }

        if (!isValid) {
            input.classList.add('is-invalid');
            this.showFieldError(input, errorMessage);
        } else {
            input.classList.remove('is-invalid');
            const feedback = input.parentNode.querySelector('.invalid-feedback');
            if (feedback && !feedback.classList.contains('d-block')) {
                feedback.remove();
            }
        }

        return isValid;
    }

    /**
     * Muestra error en un campo
     * @param {HTMLElement} input - Campo
     * @param {string} message - Mensaje de error
     */
    showFieldError(input, message) {
        let feedback = input.parentNode.querySelector('.invalid-feedback');
        if (!feedback) {
            feedback = document.createElement('div');
            feedback.className = 'invalid-feedback d-block';
            input.parentNode.appendChild(feedback);
        }
        feedback.textContent = message;
    }

    /**
     * Maneja el envío del formulario
     * @param {Event} e - Evento del formulario
     */
    async handleSubmit(e) {
        e.preventDefault();

        const form = document.getElementById(this.formId);
        const email = form.querySelector('input[name="email"]').value.trim();
        const password = form.querySelector('input[name="password"]').value.trim();

        // Validar todos los campos
        if (!this.validateField(form.querySelector('input[name="email"]')) ||
            !this.validateField(form.querySelector('input[name="password"]'))) {
            return;
        }

        // Mostrar spinner de carga
        this.showLoading(true);

        try {
            // Llamar al servicio de autenticación
            const result = await AuthService.login(email, password);

            if (result.success) {
                // Mostrar mensaje de éxito
                Utils.showMessage(
                    'Success',
                    `¡Welcome, ${AuthService.getFullName()}! Login successful.`,
                    'success'
                );

                // Redirigir al dashboard
                setTimeout(() => {
                    App.getInstance().navigateTo('/dashboard');
                }, 1000);
            } else {
                // Mostrar error del servidor
                Utils.showMessage(
                    'Login Failed',
                    result.message || 'Invalid email or password',
                    'error'
                );
            }
        } catch (error) {
            console.error('Error en login:', error);
            
            let errorMessage = 'An error occurred during login. Please try again.';
            
            if (error.message.includes('CORS')) {
                errorMessage = 'Connection error. The API server may not be running.';
            } else if (error.message.includes('HTTP 401')) {
                errorMessage = 'Invalid email or password.';
            }

            Utils.showMessage('Error', errorMessage, 'error');
        } finally {
            // Ocultar spinner de carga
            this.showLoading(false);
        }
    }

    /**
     * Muestra/oculta el spinner de carga
     * @param {boolean} show - Mostrar/ocultar
     */
    showLoading(show) {
        const spinner = document.getElementById('loadingSpinner');
        const btn = document.getElementById('loginBtn');
        
        if (show) {
            spinner.style.display = 'block';
            btn.disabled = true;
        } else {
            spinner.style.display = 'none';
            btn.disabled = false;
        }
    }
}

// Exponer globalmente para la verificación de carga
window.LoginComponent = LoginComponent;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') AppScripts.register('login');
