/**
 * constants.js - Constantes de la aplicación
 */

const API_CONFIG = {
    // API en HTTPS mediante proxy nginx: http://localhost/api → https://localhost:7200/api (ASP.NET)
    BASE_URL: '/api',
    TIMEOUT: 5000,
    ENDPOINTS: {
        REGISTER: '/user/register',
        LOGIN: '/auth/login',
        USERS: '/user',
        USER_BY_ID: '/user/:id',
        UPDATE_USER: '/user/:id',
        DELETE_USER: '/user/:id'
    }
};

const ROUTES = {
    HOME: '/',
    REGISTER: '/register',
    LOGIN: '/login',
    USERS: '/users',
    DASHBOARD: '/dashboard'
};

const MESSAGES = {
    SUCCESS: {
        REGISTER: '¡User registration successful! Your account has been created.',
        LOGIN: '¡Welcome! You have logged in successfully.',
        UPDATE: 'The user has been updated correctly.',
        DELETE: 'The user has been deleted correctly.'
    },
    ERROR: {
        REGISTER_FAILED: 'Error registering the account. Please try again.',
        LOGIN_FAILED: 'Invalid credentials. Please verify your username and password.',
        SERVER_ERROR: 'Server error. Please try later.',
        VALIDATION_ERROR: 'Please complete all fields correctly.',
        EMAIL_EXISTS: 'The email address is already registered.',
        NICK_EXISTS: 'The username (nick) is already taken.',
        CONNECTION_ERROR: 'Connection error. Please verify your internet connection.'
    }
};

const VALIDATION_PATTERNS = {
    EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    PHONE: /^[+]?[0-9]{7,15}$/,
    PASSWORD: /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_.\-*])/, // Debe tener mayúscula, minúscula, número y carácter especial
    NAME: /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]{2,255}$/,
    NICK: /^[a-zA-Z0-9_]{3,255}$/ // Alfanumérico y guión bajo, mínimo 3
};

// Exponer globalmente para la verificación de carga
window.API_CONFIG = API_CONFIG;
window.ROUTES = ROUTES;
window.MESSAGES = MESSAGES;
window.VALIDATION_PATTERNS = VALIDATION_PATTERNS;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') AppScripts.register('constants');
