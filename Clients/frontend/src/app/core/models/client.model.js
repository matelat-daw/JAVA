/**
 * user.model.js - Modelo del Usuario
 */

class User {
    constructor(data = {}) {
        this.id = data.id || null;
        this.nick = data.nick || '';
        this.name = data.name || '';
        this.surname1 = data.surname1 || '';
        this.surname2 = data.surname2 || '';
        this.bday = data.bday || '';
        this.phone = data.phone || '';
        this.gender = data.gender || '';
        this.email = data.email || '';
        this.password = data.password || '';
        this.profileImg = data.profileImg || null;
        this.active = data.active || false;
        this.emailVerified = data.emailVerified || false;
        this.verificationToken = data.verificationToken || null;
        this.verificationTokenExpiry = data.verificationTokenExpiry || null;
        this.createdAt = data.createdAt || null;
        this.updatedAt = data.updatedAt || null;
        this.role = data.role || 'USER';
    }

    /**
     * Valida los datos del usuario para registro
     * @returns {Object} {valid: boolean, errors: Object}
     */
    validateForRegistration() {
        const rules = {
            nick: {
                required: true,
                label: 'Username',
                pattern: VALIDATION_PATTERNS.NICK,
                message: 'Username must be alphanumeric (3-255 characters, underscore allowed)'
            },
            name: {
                required: true,
                label: 'First Name',
                pattern: VALIDATION_PATTERNS.NAME,
                message: 'First name must contain only letters'
            },
            surname1: {
                required: true,
                label: 'First Surname',
                pattern: VALIDATION_PATTERNS.NAME,
                message: 'First surname must contain only letters'
            },
            surname2: {
                required: false,
                label: 'Second Surname',
                pattern: VALIDATION_PATTERNS.NAME,
                message: 'Second surname must contain only letters'
            },
            email: {
                required: true,
                label: 'Email',
                pattern: VALIDATION_PATTERNS.EMAIL,
                message: 'The email address is not valid'
            },
            phone: {
                required: true,
                label: 'Phone',
                pattern: VALIDATION_PATTERNS.PHONE,
                message: 'Phone must contain only numbers (7-15 digits)'
            },
            password: {
                required: true,
                label: 'Password',
                minLength: 6,
                message: 'Password must be at least 6 characters'
            },
            gender: {
                required: true,
                label: 'Gender'
            },
            bday: {
                required: false,
                label: 'Birth Date'
            }
        };

        return Utils.validateForm({
            nick: this.nick,
            name: this.name,
            surname1: this.surname1,
            surname2: this.surname2,
            email: this.email,
            phone: this.phone,
            password: this.password,
            gender: this.gender,
            bday: this.bday
        }, rules);
    }

    /**
     * Convierte el usuario a JSON para enviar a la API
     * @returns {Object}
     */
    toJSON() {
        return {
            nick: this.nick,
            name: this.name,
            surname1: this.surname1,
            surname2: this.surname2 || null,
            bday: this.bday || null,
            phone: this.phone,
            gender: this.gender,
            email: this.email,
            password: this.password
        };
    }

    /**
     * Crea un usuario a partir de un objeto JSON
     * @param {Object} json
     * @returns {User}
     */
    static fromJSON(json) {
        return new User(json);
    }
}

// Exponer globalmente para la verificación de carga
window.User = User;
window.Client = User;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') AppScripts.register('client.model');
