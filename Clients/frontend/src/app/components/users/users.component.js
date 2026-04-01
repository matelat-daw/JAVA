/**
 * UsersComponent.js - Componente de gestión de usuarios
 */

class UsersComponent {
    constructor() {
        this.selector = '#router-outlet';
        this.currentPage = 0;
        this.pageSize = 10;
        this.users = [];
        this.totalItems = 0;
        this.totalPages = 0;
    }

    async init() {
        try {
            console.log('🔄 Inicializando componente de usuarios...');

            // Verificar si el usuario está autenticado
            if (!AuthService.isAuthenticated()) {
                console.warn('Usuario no autenticado. Redirigiendo a login...');
                App.getInstance().navigateTo('/login');
                return;
            }

            // Obtener el rol del usuario
            const userRole = AuthService.getRole();
            console.log('User role:', userRole);
            const isAdmin = userRole === 'ADMIN';

            if (!isAdmin) {
                this.renderAccessDenied(userRole);
                return;
            }

            // Mostrar spinner mientras se cargan los datos
            const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = `
                    <div class="text-center py-5">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Cargando usuarios...</span>
                        </div>
                        <p class="mt-3">Cargando lista de usuarios...</p>
                    </div>
                `;
            }

            // Cargar usuarios
            await this.loadUsers();

            // Renderizar tabla
            this.renderAdminView();

            // Adjuntar event listeners para paginación
            this.attachPaginationListeners();

            console.log('✅ Componente de usuarios inicializado correctamente');
        } catch (error) {
            console.error('❌ Error al cargar usuarios:', error);
            const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = `
                    <div class="alert alert-danger m-5">
                        <h4>Error al cargar usuarios</h4>
                        <p>${error.message || 'No se pudo cargar la lista de usuarios'}</p>
                        <button class="btn btn-primary" onclick="location.reload()">Recargar</button>
                    </div>
                `;
            }
        }
    }

    async loadUsers() {
        try {
            console.log(`📥 Cargando usuarios (página ${this.currentPage + 1}, tamaño ${this.pageSize})...`);
            const response = await UserService.getUsers(this.currentPage, this.pageSize);
            
            if (response.success) {
                this.users = response.users || [];
                const pagination = response.pagination || {};
                
                this.totalItems = pagination.totalItems || 0;
                this.totalPages = pagination.totalPages || 0;
                
                console.log(`✅ Usuarios cargados: ${this.users.length} de ${this.totalItems} total`);
            } else {
                throw new Error(response.message || 'Error al obtener usuarios');
            }
        } catch (error) {
            console.error('Error loading users:', error);
            throw error;
        }
    }

    renderAdminView() {
        const currentUser = AuthService.getUserSession();
        const userName = currentUser ? `${currentUser.name} ${currentUser.surname1}` : 'Usuario';
        
        const html = `
            <div class="container-fluid py-5">
                <!-- Saludo Usuario -->
                <div class="row mb-4">
                    <div class="col-12">
                        <div class="alert alert-info alert-dismissible fade show" role="alert">
                            <i class="fas fa-user-check me-2"></i>
                            <strong>¡Bienvenido Usuario: ${userName}!</strong>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </div>
                </div>

                <!-- Header -->
                <div class="row mb-4">
                    <div class="col-12">
                        <h1 class="display-5 fw-bold mb-2">
                            <i class="fas fa-users me-3"></i>Lista de Usuarios
                        </h1>
                        <p class="text-muted">Total de usuarios: <strong>${this.totalItems}</strong></p>
                    </div>
                </div>

                <!-- Tabla de usuarios -->
                <div class="row">
                    <div class="col-12">
                        <div class="card shadow-sm">
                            <div class="card-body">
                                ${this.users.length > 0 ? this.renderTable() : this.renderEmpty()}
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Paginación -->
                ${this.totalPages > 1 ? this.renderPagination() : ''}
            </div>
        `;

        const container = document.querySelector(this.selector);
        if (container) {
            container.innerHTML = html;
        }
    }

    renderTable() {
        const rows = this.users.map(user => `
            <tr>
                <td>
                    ${user.profileImg ? 
                        `<img src="/api/images/${user.profileImg}" alt="${user.nick}" class="img-thumbnail rounded-circle" style="width: 40px; height: 40px; object-fit: cover;">` :
                        `<div class="bg-secondary rounded-circle d-inline-flex align-items-center justify-content-center" style="width: 40px; height: 40px;"><i class="fas fa-user text-white"></i></div>`
                    }
                </td>
                <td>
                    <strong>${user.nick || 'N/A'}</strong><br>
                    <small class="text-muted">${user.name} ${user.surname1}</small>
                </td>
                <td>${user.email}</td>
                <td>${user.phone || '-'}</td>
                <td>
                    <span class="badge ${this.getRoleBadgeClass(user.role) || 'bg-secondary'}">
                        ${user.role || 'USER'}
                    </span>
                </td>
                <td>
                    <small class="text-muted">${new Date(user.createdAt).toLocaleDateString()}</small>
                </td>
                <td>
                    <div class="btn-group btn-group-sm" role="group">
                        <button class="btn btn-outline-primary btn-sm" onclick="App.getInstance().navigateTo('/users/${user.id}')">
                            <i class="fas fa-eye"></i> Ver
                        </button>
                        <button class="btn btn-outline-danger btn-sm" onclick="UsersComponent.confirmDelete(${user.id}, '${user.nick}')">
                            <i class="fas fa-trash"></i> Eliminar
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');

        return `
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead class="table-light">
                        <tr>
                            <th style="width: 50px;">Foto</th>
                            <th>Usuario</th>
                            <th>Email</th>
                            <th>Teléfono</th>
                            <th>Rol</th>
                            <th>Registro</th>
                            <th style="width: 150px;">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${rows}
                    </tbody>
                </table>
            </div>
        `;
    }

    renderEmpty() {
        return `
            <div class="text-center py-5">
                <i class="fas fa-inbox display-1 text-secondary mb-3"></i>
                <h4>No hay usuarios</h4>
                <p class="text-muted">No se encontraron usuarios en el sistema.</p>
            </div>
        `;
    }

    renderPagination() {
        const pages = [];
        
        // Boton anterior
        pages.push(`
            <li class="page-item ${this.currentPage === 0 ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0)" onclick="UsersComponent.previousPage()" data-page="${this.currentPage - 1}">
                    Anterior
                </a>
            </li>
        `);

        // Números de página
        for (let i = 0; i < this.totalPages; i++) {
            pages.push(`
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0)" onclick="UsersComponent.goToPage(${i})" data-page="${i}">
                        ${i + 1}
                    </a>
                </li>
            `);
        }

        // Boton siguiente
        pages.push(`
            <li class="page-item ${this.currentPage === this.totalPages - 1 ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0)" onclick="UsersComponent.nextPage()" data-page="${this.currentPage + 1}">
                    Siguiente
                </a>
            </li>
        `);

        return `
            <div class="row mt-5">
                <div class="col-12 d-flex justify-content-center">
                    <nav>
                        <ul class="pagination">
                            ${pages.join('')}
                        </ul>
                    </nav>
                </div>
            </div>
        `;
    }

    renderAccessDenied(userRole) {
        const roleBadgeClass = this.getRoleBadgeClass(userRole);
        const html = `
            <div class="container py-5">
                <div class="row mb-5">
                    <div class="col-12">
                        <h1 class="display-5 fw-bold mb-2"><i class="fas fa-users me-3"></i>Lista de Usuarios</h1>
                        <p class="text-muted">Administra los usuarios del sistema</p>
                    </div>
                </div>
                <div class="row justify-content-center">
                    <div class="col-md-8">
                        <div class="card shadow-sm border-warning">
                            <div class="card-body text-center py-5">
                                <div style="font-size: 80px; margin-bottom: 20px;">🔐</div>
                                <h2 class="card-title fw-bold mb-3 text-danger">Acceso Denegado</h2>
                                <p class="lead mb-3">No tienes permisos para acceder a esta sección</p>
                                <p class="text-muted mb-4">
                                    <i class="fas fa-info-circle me-2"></i>
                                    <strong>Esta funcionalidad solo está disponible para usuarios con rol ADMIN</strong>
                                </p>
                                <div class="alert alert-info" role="alert">
                                    <i class="fas fa-user-circle me-2"></i>
                                    <strong>Tu rol actual:</strong> <span class="badge ${roleBadgeClass} ms-2">${userRole || 'No especificado'}</span>
                                </div>
                                <a href="/dashboard" class="btn btn-primary mt-3">
                                    <i class="fas fa-arrow-left me-2"></i>Volver al Dashboard
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        const container = document.querySelector(this.selector);
        if (container) {
            container.innerHTML = html;
        }
    }

    attachPaginationListeners() {
        // Los listeners ya están adjuntos en los botones
    }

    getRoleBadgeClass(role) {
        const roleBadgeMap = {
            'ADMIN': 'bg-danger',
            'USER': 'bg-info',
            'MODERATOR': 'bg-warning',
            'GUEST': 'bg-secondary'
        };
        return roleBadgeMap[role] || 'bg-secondary';
    }

    /**
     * Confirma y elimina un usuario
     */
    static async confirmDelete(userId, userNick) {
        // Crear modal de confirmación
        const modalHtml = `
            <div class="modal fade" id="deleteUserModal" tabindex="-1" aria-labelledby="deleteUserLabel" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header bg-danger text-white">
                            <h5 class="modal-title" id="deleteUserLabel">
                                <i class="fas fa-trash me-2"></i>Confirmar Eliminación
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <div class="alert alert-warning mb-3" role="alert">
                                <i class="fas fa-exclamation-triangle me-2"></i>
                                <strong>¡Advertencia!</strong> Esta acción es irreversible.
                            </div>
                            <p>¿Estás seguro de que deseas eliminar al usuario <strong>${userNick}</strong>?</p>
                            <p class="text-muted"><small>Una vez eliminado, no se puede recuperar.</small></p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-danger" onclick="UsersComponent.deleteUserConfirmed(${userId})">
                                <i class="fas fa-trash me-2"></i>Sí, eliminar
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Remover modal anterior si existe
        const existingModal = document.getElementById('deleteUserModal');
        if (existingModal) {
            existingModal.remove();
        }

        // Agregar nuevo modal
        document.body.insertAdjacentHTML('beforeend', modalHtml);

        // Mostrar modal
        const modal = new bootstrap.Modal(document.getElementById('deleteUserModal'));
        modal.show();
    }

    /**
     * Ejecuta la eliminación del usuario confirmada
     */
    static async deleteUserConfirmed(userId) {
        try {
            const deleteBtn = document.querySelector('.btn-danger[onclick*="deleteUserConfirmed"]');
            if (deleteBtn) {
                deleteBtn.disabled = true;
                deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Eliminando...';
            }

            console.log('🗑️ Eliminando usuario ID:', userId);
            const response = await UserService.deleteUser(userId);

            if (response.success) {
                console.log('✅ Usuario eliminado exitosamente');

                // Cerrar modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('deleteUserModal'));
                if (modal) {
                    modal.hide();
                }

                // Mostrar notificación de éxito
                this.showSuccessNotification('Usuario eliminado exitosamente');

                // Recargar lista de usuarios de forma más simple
                console.log('🔄 Recargando lista de usuarios...');
                setTimeout(async () => {
                    try {
                        // Acceder a la instancia global
                        const instance = window.UsersComponentInstance;
                        
                        if (instance) {
                            console.log('📥 Recargando desde instancia existente');
                            // Resetear a primera página
                            instance.currentPage = 0;
                            await instance.loadUsers();
                            instance.renderAdminView();
                            instance.attachPaginationListeners();
                        } else {
                            console.warn('⚠️ Instancia no disponible, navegando a /users');
                            // Si no hay instancia, navegar a /users para forzar recarga
                            App.getInstance().navigateTo('/users');
                        }
                    } catch (error) {
                        console.error('❌ Error al recargar:', error);
                        // Fallback: Navegar a /users
                        App.getInstance().navigateTo('/users');
                    }
                }, 800);

            } else {
                console.error('❌ Error al eliminar usuario:', response.message);
                this.showErrorNotification(response.message || 'Error al eliminar usuario');
                
                // Habilitar botón nuevamente
                if (deleteBtn) {
                    deleteBtn.disabled = false;
                    deleteBtn.innerHTML = '<i class="fas fa-trash me-2"></i>Sí, eliminar';
                }
            }
        } catch (error) {
            console.error('❌ Error en eliminación:', error);
            this.showErrorNotification('Error al eliminar usuario: ' + error.message);
            
            const deleteBtn = document.querySelector('.btn-danger[onclick*="deleteUserConfirmed"]');
            if (deleteBtn) {
                deleteBtn.disabled = false;
                deleteBtn.innerHTML = '<i class="fas fa-trash me-2"></i>Sí, eliminar';
            }
        }
    }

    /**
     * Muestra notificación de éxito
     */
    static showSuccessNotification(message) {
        const alertHtml = `
            <div class="alert alert-success alert-dismissible fade show position-fixed" role="alert" 
                 style="top: 20px; right: 20px; z-index: 9999; min-width: 300px;">
                <i class="fas fa-check-circle me-2"></i>
                <strong>Éxito!</strong> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', alertHtml);

        // Auto-hide después de 3 segundos
        setTimeout(() => {
            const alerts = document.querySelectorAll('.alert-success.position-fixed');
            alerts.forEach(alert => alert.remove());
        }, 3000);
    }

    /**
     * Muestra notificación de error
     */
    static showErrorNotification(message) {
        const alertHtml = `
            <div class="alert alert-danger alert-dismissible fade show position-fixed" role="alert" 
                 style="top: 20px; right: 20px; z-index: 9999; min-width: 300px;">
                <i class="fas fa-exclamation-circle me-2"></i>
                <strong>Error!</strong> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', alertHtml);

        // Auto-hide después de 5 segundos
        setTimeout(() => {
            const alerts = document.querySelectorAll('.alert-danger.position-fixed');
            alerts.forEach(alert => alert.remove());
        }, 5000);
    }

    // Métodos de paginación estáticos
    static async goToPage(page) {
        const instance = UsersComponentInstance;
        if (instance) {
            instance.currentPage = page;
            await instance.loadUsers();
            instance.renderAdminView();
            instance.attachPaginationListeners();
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    }

    static async nextPage() {
        const instance = UsersComponentInstance;
        if (instance && instance.currentPage < instance.totalPages - 1) {
            await this.goToPage(instance.currentPage + 1);
        }
    }

    static async previousPage() {
        const instance = UsersComponentInstance;
        if (instance && instance.currentPage > 0) {
            await this.goToPage(instance.currentPage - 1);
        }
    }
}

// Instancia global para acceder desde métodos estáticos
let UsersComponentInstance = null;

// Exponer globalmente para la verificación de carga
window.UsersComponent = UsersComponent;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') {
    AppScripts.register('users');
}
