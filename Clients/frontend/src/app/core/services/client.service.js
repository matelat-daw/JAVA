/**
 * user.service.js - Servicio para gestionar usuarios
 */

class UserService {
    /**
     * Registra un nuevo usuario
     * @param {User} user - Objeto usuario
     * @param {File} profilePicture - Archivo de imagen de perfil (opcional)
     * @returns {Promise<Object>}
     */
    static async register(user, profilePicture = null) {
        try {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.REGISTER;
            
            // Crear FormData para enviar datos + archivo
            const formData = new FormData();
            
            // Agregar todos los campos del usuario
            formData.append('nick', user.nick);
            formData.append('name', user.name);
            formData.append('surname1', user.surname1);
            if (user.surname2) formData.append('surname2', user.surname2);
            formData.append('email', user.email);
            formData.append('phone', user.phone);
            formData.append('password', user.password);
            formData.append('gender', user.gender);
            if (user.bday) formData.append('bday', user.bday);
            
            // Agregar archivo de imagen si existe
            if (profilePicture) {
                formData.append('profilePicture', profilePicture, profilePicture.name);
            }
            
            console.log('📤 Enviando registro con FormData a:', url);
            
            // Enviar como FormData
            const response = await Utils.makeRequestWithFormData('POST', url, formData);
            return response;
        } catch (error) {
            console.error('Error in registration:', error);
            throw error;
        }
    }

    /**
     * Obtiene todos los usuarios con paginación
     * @param {number} page - Número de página (default 0)
     * @param {number} size - Tamaño de página (default 10)
     * @returns {Promise<Object>}
     */
    static async getUsers(page = 0, size = 10) {
        try {
            const url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.USERS}?page=${page}&size=${size}`;
            const response = await Utils.makeRequest('GET', url);
            return response;
        } catch (error) {
            console.error('Error getting users:', error);
            throw error;
        }
    }

    /**
     * Obtiene un usuario por ID
     * @param {number} id - ID del usuario
     * @returns {Promise<Object>}
     */
    static async getUserById(id) {
        try {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.USER_BY_ID.replace(':id', id);
            const response = await Utils.makeRequest('GET', url);
            return response;
        } catch (error) {
            console.error('Error getting user:', error);
            throw error;
        }
    }

    /**
     * Actualiza un usuario
     * @param {number} id - ID del usuario
     * @param {User} user - Objeto usuario con datos actualizados
     * @returns {Promise<Object>}
     */
    static async updateUser(id, user) {
        try {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.UPDATE_USER.replace(':id', id);
            const response = await Utils.makeRequest('PUT', url, user.toJSON());
            return response;
        } catch (error) {
            console.error('Error updating user:', error);
            throw error;
        }
    }

    /**
     * Elimina un usuario
     * @param {number} id - ID del usuario
     * @returns {Promise<Object>}
     */
    static async deleteUser(id) {
        try {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.DELETE_USER.replace(':id', id);
            const response = await Utils.makeRequest('DELETE', url);
            return response;
        } catch (error) {
            console.error('Error deleting user:', error);
            throw error;
        }
    }
}

// Exponer globalmente para la verificación de carga
window.UserService = UserService;
window.ClientService = UserService;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') AppScripts.register('client.service');
