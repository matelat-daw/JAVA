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
        const html = `
            <div class="container-fluid py-5">
                <!-- Header -->
                <div class="row mb-5">
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
                    ${user.profilePicture ? 
                        `<img src="/profile-pictures/${user.profilePicture}" alt="${user.nick}" class="img-thumbnail rounded-circle" style="width: 40px; height: 40px; object-fit: cover;">` :
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
                        <button class="btn btn-outline-danger btn-sm" onclick="if(confirm('¿Eliminar usuario?')) alert('Funcionalidad pendiente')">
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
