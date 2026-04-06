/**
 * register.component.js - Componente de Registro
 */

class RegisterComponent {
    constructor() {
        this.selector = '#router-outlet';
        this.formId = 'registerForm';
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

            const response = await fetch('/frontend/src/app/components/register/register.component.html');
            const html = await response.text();
            
            const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = html;
                
                // Adjuntar eventos
                this.attachEventListeners();
            }
        } catch (error) {
            console.error('Error al cargar formulario de registro:', error);
            Utils.showMessage('Error', 'No se pudo cargar el formulario de registro', 'error');
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
        const inputs = form.querySelectorAll('input[type="text"], input[type="email"], input[type="tel"], input[type="password"], select, textarea');
        inputs.forEach(input => {
            input.addEventListener('blur', () => this.validateField(input));
            input.addEventListener('input', () => {
                if (input.classList.contains('is-invalid')) {
                    this.validateField(input);
                }
            });
        });

        // Manejo de preview de imagen de perfil
        const profilePictureInput = form.querySelector('input[name="profilePicture"]');
        if (profilePictureInput) {
            profilePictureInput.addEventListener('change', (e) => this.handleImagePreview(e));
        }

        // Validación de contraseña coincidente
        const passwordInput = form.querySelector('input[name="password"]');
        const confirmPasswordInput = form.querySelector('input[name="confirmPassword"]');
        
        if (confirmPasswordInput) {
            confirmPasswordInput.addEventListener('blur', () => {
                if (passwordInput.value !== confirmPasswordInput.value) {
                    confirmPasswordInput.classList.add('is-invalid');
                    this.showFieldError(confirmPasswordInput, 'Passwords do not match');
                } else {
                    confirmPasswordInput.classList.remove('is-invalid');
                    const feedback = confirmPasswordInput.parentNode.querySelector('.invalid-feedback');
                    if (feedback) {
                        feedback.remove();
                    }
                }
            });
        }
    }

    /**
     * Maneja el preview de la imagen de perfil
     * @param {Event} e - Evento del input file
     */
    handleImagePreview(e) {
        const file = e.target.files[0];
        const previewContainer = document.getElementById('imagePreview');
        const previewImage = document.getElementById('previewImage');

        if (!file) {
            previewContainer.style.display = 'none';
            return;
        }

        // Validar tipo de archivo
        const validTypes = ['image/png', 'image/jpeg', 'image/jpg', 'image/gif', 'image/webp'];
        if (!validTypes.includes(file.type)) {
            Utils.showMessage('Invalid Image', 'Please select a valid image format (PNG, JPG, GIF, WebP)', 'warning');
            e.target.value = '';
            previewContainer.style.display = 'none';
            return;
        }

        // Validar tamaño (máximo 20MB)
        const maxSize = 20 * 1024 * 1024; // 20MB
        if (file.size > maxSize) {
            Utils.showMessage('File Too Large', 'Image size must be less than 20MB', 'warning');
            e.target.value = '';
            previewContainer.style.display = 'none';
            return;
        }

        // Mostrar preview
        const reader = new FileReader();
        reader.onload = (event) => {
            previewImage.src = event.target.result;
            previewContainer.style.display = 'block';
        };
        reader.readAsDataURL(file);
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
            case 'nick':
                if (!value) {
                    isValid = false;
                    errorMessage = 'Username is required';
                } else if (!VALIDATION_PATTERNS.NICK.test(value)) {
                    isValid = false;
                    errorMessage = 'Username must be 3-255 characters alphanumeric (underscore allowed)';
                }
                break;

            case 'name':
                if (!value) {
                    isValid = false;
                    errorMessage = 'First name is required';
                } else if (!VALIDATION_PATTERNS.NAME.test(value)) {
                    isValid = false;
                    errorMessage = 'First name must contain only letters and spaces';
                }
                break;

            case 'surname1':
                if (!value) {
                    isValid = false;
                    errorMessage = 'First surname is required';
                } else if (!VALIDATION_PATTERNS.NAME.test(value)) {
                    isValid = false;
                    errorMessage = 'First surname must contain only letters and spaces';
                }
                break;

            case 'surname2':
                if (value && !VALIDATION_PATTERNS.NAME.test(value)) {
                    isValid = false;
                    errorMessage = 'Second surname must contain only letters and spaces';
                }
                break;

            case 'email':
                if (!value) {
                    isValid = false;
                    errorMessage = 'Email is required';
                } else if (!VALIDATION_PATTERNS.EMAIL.test(value)) {
                    isValid = false;
                    errorMessage = 'Email address is not valid';
                }
                break;

            case 'phone':
                if (!value) {
                    isValid = false;
                    errorMessage = 'Phone is required';
                } else if (!VALIDATION_PATTERNS.PHONE.test(value)) {
                    isValid = false;
                    errorMessage = 'Phone must contain 7-15 numbers';
                }
                break;

            case 'password':
                if (!value) {
                    isValid = false;
                    errorMessage = 'Password is required';
                } else if (value.length < 8) {
                    isValid = false;
                    errorMessage = 'Password must be at least 8 characters';
                } else if (!VALIDATION_PATTERNS.PASSWORD.test(value)) {
                    isValid = false;
                    errorMessage = 'Password must contain: uppercase, lowercase, number, and special character (@#$%^&+=!_.-*)';
                }
                break;

            case 'confirmPassword':
                const passwordInput = document.querySelector('input[name="password"]');
                if (!value) {
                    isValid = false;
                    errorMessage = 'You must confirm your password';
                } else if (value !== passwordInput.value) {
                    isValid = false;
                    errorMessage = 'Passwords do not match';
                }
                break;

            case 'gender':
                if (!value) {
                    isValid = false;
                    errorMessage = `${this.getFieldLabel(name)} is required`;
                }
                break;

            case 'bday':
                // Birth date is optional, no validation needed if empty
                break;
        }

        if (isValid) {
            input.classList.remove('is-invalid');
            const feedback = input.parentNode.querySelector('.invalid-feedback');
            if (feedback) {
                feedback.remove();
            }
        } else {
            input.classList.add('is-invalid');
            this.showFieldError(input, errorMessage);
        }

        return isValid;
    }

    /**
     * Obtiene la etiqueta del campo
     * @param {string} fieldName - Nombre del campo
     * @returns {string}
     */
    getFieldLabel(fieldName) {
        const labels = {
            nick: 'Username',
            name: 'First Name',
            surname1: 'First Surname',
            surname2: 'Second Surname',
            email: 'Email',
            phone: 'Phone',
            password: 'Password',
            confirmPassword: 'Confirm Password',
            gender: 'Gender',
            bday: 'Birth Date',
            profilePicture: 'Profile Picture'
        };
        return labels[fieldName] || fieldName;
    }

    /**
     * Muestra el error de un campo
     * @param {HTMLElement} input - Campo con error
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
     * Valida todo el formulario
     * @returns {boolean}
     */
    validateForm() {
        const form = document.getElementById(this.formId);
        if (!form) return false;

        const inputs = form.querySelectorAll('input[type="text"], input[type="email"], input[type="tel"], input[type="password"], input[type="date"], select');
        let isFormValid = true;

        inputs.forEach(input => {
            // Saltar campos opcionales si están vacíos
            if ((input.name === 'surname2' || input.name === 'bday') && !input.value.trim()) {
                return; // skip
            }
            
            if (input.name !== 'confirmPassword') {
                if (!this.validateField(input)) {
                    isFormValid = false;
                }
            }
        });

        // Validar contraseñas coincidentes
        const passwordInput = form.querySelector('input[name="password"]');
        const confirmPasswordInput = form.querySelector('input[name="confirmPassword"]');
        if (confirmPasswordInput && passwordInput.value !== confirmPasswordInput.value) {
            this.showFieldError(confirmPasswordInput, 'Passwords do not match');
            isFormValid = false;
        }

        // Validar términos
        const termsCheckbox = form.querySelector('input[name="terms"]');
        if (termsCheckbox && !termsCheckbox.checked) {
            Utils.showMessage('Validation', 'You must accept the terms and conditions', 'warning');
            isFormValid = false;
        }

        return isFormValid;
    }

    /**
     * Maneja el envío del formulario
     * @param {Event} e - Evento del formulario
     */
    async handleSubmit(e) {
        e.preventDefault();

        // Validar formulario primero (sin deshabilitar botón)
        const initialValidation = this.validateForm();
        
        if (!initialValidation) {
            Utils.showMessage('Validation', 'Please complete all fields correctly', 'warning');
            return; // Salir sin deshabilitar el botón
        }

        // Obtener datos del formulario
        const form = document.getElementById(this.formId);
        const formDataObj = new FormData(form);
        
        const userData = {
            nick: formDataObj.get('nick').trim(),
            name: formDataObj.get('name').trim(),
            surname1: formDataObj.get('surname1').trim(),
            surname2: formDataObj.get('surname2') ? formDataObj.get('surname2').trim() : null,
            email: formDataObj.get('email').trim(),
            phone: formDataObj.get('phone').trim(),
            password: formDataObj.get('password'),
            gender: formDataObj.get('gender'),
            bday: formDataObj.get('bday') ? formDataObj.get('bday') : null
        };

        // Crear objeto User
        const user = new User(userData);

        // Obtener archivo de imagen si existe
        const profilePictureFile = formDataObj.get('profilePicture');

        // Mostrar indicador de carga AQUÍ, después de validación exitosa
        const submitBtn = form.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Registering...';

        try {
            // Llamar al servicio de registro (con imagen si existe)
            const response = await UserService.register(user, profilePictureFile);
            
            console.log('Registration response:', response);

            // Mostrar mensaje de éxito
            Utils.showMessage(
                'Registration Successful',
                MESSAGES.SUCCESS.REGISTER + '<br><br><strong>Your ID is:</strong> ' + (response.user?.id || response.id),
                'success'
            );

            // Limpiar formulario después de 2 segundos
            setTimeout(() => {
                Utils.clearForm(this.formId);
                // Restaurar botón primero
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
                // Redirigir a login o inicio
                Utils.navigate(ROUTES.HOME);
            }, 2000);

        } catch (error) {
            console.error('Error in registration:', error);
            
            let errorMessage = MESSAGES.ERROR.REGISTER_FAILED;
            let errorTitle = 'Registration Error';
            
            // Detectar tipo de error específico
            if (error.message.includes('CORS_ERROR')) {
                errorTitle = '⚠️ API Connection Issue';
                errorMessage = 'No API available. Nginx tried to connect to:<br>' +
                              '1. <code>http://localhost:8080</code> (primary)<br>' +
                              '2. <code>https://localhost:7200</code> (fallback)<br><br>' +
                              '<strong>Solution:</strong><br>' +
                              '• Make sure at least one API is running<br>' +
                              '• Check that CORS is configured in the backend';
            } else if (error.message.includes('Failed to fetch')) {
                errorTitle = '🔌 Connection Error';
                errorMessage = 'Cannot connect to any API server.<br><br>' +
                              '<strong>Make sure:</strong><br>' +
                              '• At least one API is running (port 8080 or 7200)<br>' +
                              '• CORS is properly configured<br>' +
                              '• Your internet connection is working';
            } else if (error.message.includes('400')) {
                errorMessage = MESSAGES.ERROR.VALIDATION_ERROR;
            } else if (error.message.includes('409')) {
                if (error.message.includes('email')) {
                    errorMessage = MESSAGES.ERROR.EMAIL_EXISTS;
                } else if (error.message.includes('nick')) {
                    errorMessage = MESSAGES.ERROR.NICK_EXISTS;
                }
            } else if (error.message.includes('500')) {
                errorMessage = MESSAGES.ERROR.SERVER_ERROR;
            } else if (error.message.includes('fetch')) {
                errorMessage = MESSAGES.ERROR.CONNECTION_ERROR;
            }

            Utils.showMessage(errorTitle, errorMessage, 'error');

        } finally {
            // Restaurar botón
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        }
    }
}

// Exponer globalmente para la verificación de carga
window.RegisterComponent = RegisterComponent;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') AppScripts.register('register');
