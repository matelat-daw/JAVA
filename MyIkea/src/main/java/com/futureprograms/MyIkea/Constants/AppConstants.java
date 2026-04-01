package com.futureprograms.MyIkea.Constants;

/**
 * Constantes de la aplicación MyIkea
 */
public class AppConstants {
    // Roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    
    // Nombres de roles sin el prefijo ROLE_
    public static final String ROLE_NAME_USER = "USER";
    public static final String ROLE_NAME_MANAGER = "MANAGER";
    public static final String ROLE_NAME_ADMIN = "ADMIN";
    
    // Rutas
    public static final String ROUTE_HOME = "/";
    public static final String ROUTE_LOGIN = "/login";
    public static final String ROUTE_REGISTER = "/register";
    public static final String ROUTE_PRODUCTOS = "/products";
    public static final String ROUTE_CARRITO = "/cart";
    public static final String ROUTE_PEDIDOS = "/orders";
    public static final String ROUTE_USERS = "/users";
    
    // Atributos de modelo
    public static final String ATTR_LOGGED = "LOGGED";
    public static final String ATTR_ADMIN = "ADMIN";
    public static final String ATTR_MANAGER = "MANAGER";
    public static final String ATTR_EMAIL = "EMAIL";
    public static final String ATTR_PRODUCTOS = "productos";
    public static final String ATTR_PRODUCTO = "producto";
    public static final String ATTR_CART = "cart";
    public static final String ATTR_ORDERS = "orders";
    public static final String ATTR_USUARIOS = "users";
    
    // Vistas
    public static final String VIEW_INDEX = "index";
    public static final String VIEW_LOGIN = "auth/login";
    public static final String VIEW_REGISTER = "auth/register";
    public static final String VIEW_PRODUCTOS_INDEX = "products/index";
    public static final String VIEW_PRODUCTOS_DETAILS = "products/details";
    public static final String VIEW_PRODUCTOS_CREATE = "products/create";
    public static final String VIEW_CART = "cart/cart";
    public static final String VIEW_ORDERS = "orders/orders";
    public static final String VIEW_ORDERS_DETAILS = "orders/details";
    public static final String VIEW_USERS = "auth/users";
    
    // Mensajes de error
    public static final String ERROR_PRODUCTO_NO_ENCONTRADO = "Producto no encontrado";
    public static final String ERROR_CART_EMPTY = "No hay productos en el carrito";
    public static final String ERROR_PEDIDO_NO_ENCONTRADO = "Pedido no encontrado";
    public static final String ERROR_USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    public static final String ERROR_SUBIR_ARCHIVO = "Error al Subir el Archivo";
    
    // Configuración de archivos
    public static final String UPLOAD_DIR = "src/main/resources/static/images";
    
    private AppConstants() {
        // Clase de constantes, no se puede instanciar
    }
}
