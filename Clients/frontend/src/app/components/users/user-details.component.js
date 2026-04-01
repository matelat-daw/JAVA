// user-details.component.js
console.log('[LOAD] user-details.component.js iniciando');

class UserDetailsComponent {
    constructor() {
        console.log('[CONSTRUCT] UserDetailsComponent constructor llamado');
        this.selector = '#router-outlet';
        this.userId = null;
        this.user = null;
    }

    async init(userId) {
        console.log('[INIT] Inicializando UserDetailsComponent con userId:', userId);
        try {
            this.userId = userId;
            
            if (!AuthService.isAuthenticated()) {
                console.log('[AUTH] Usuario no autenticado');
                App.getInstance().navigateTo('/login');
                return;
            }
            
            if (AuthService.getRole() !== 'ADMIN') {
                console.log('[AUTH] Usuario no es admin');
                this.show('ERROR: Solo admin puede ver esto');
                return;
            }
            
            this.show('Cargando...');
            await this.loadUser();
            this.show(this.render());
            this.setupListeners();
        } catch (e) {
            console.error('[INIT ERROR]', e);
            this.show('ERROR: ' + e.message);
        }
    }

    show(html) {
        const el = document.querySelector(this.selector);
        if (el) el.innerHTML = html;
    }

    async loadUser() {
        console.log('[LOAD] Cargando usuario con ID:', this.userId);
        const res = await UserService.getUserById(this.userId);
        this.user = res && res.data ? res.data : res.user;
        if (!this.user) throw new Error('Usuario no encontrado');
        console.log('[LOAD] Usuario cargado:', this.user.nick);
    }

    render() {
        console.log('[RENDER] Renderizando detalles del usuario');
        const admin = AuthService.getUserSession();
        const name = admin ? admin.name + ' ' + admin.surname1 : 'Admin';
        
        return `
<div class="container mt-4">
  <div class="alert alert-info">
    Bienvenido ${name}
  </div>
  
  <div class="row">
    <div class="col-md-8">
      <div class="card mb-3">
        <div class="card-header">
          <h5>Detalles del Usuario</h5>
        </div>
        <div class="card-body">
          <p><strong>Nick:</strong> ${this.user.nick}</p>
          <p><strong>ID:</strong> ${this.user.id}</p>
          <p><strong>Nombre:</strong> ${this.user.name}</p>
          <p><strong>Apellido 1:</strong> ${this.user.surname1}</p>
          <p><strong>Apellido 2:</strong> ${this.user.surname2 || 'N/A'}</p>
          <p><strong>Email:</strong> <a href="mailto:${this.user.email}">${this.user.email}</a></p>
          <p><strong>Telefono:</strong> ${this.user.phone || 'N/A'}</p>
          <p><strong>Rol:</strong> ${this.user.role}</p>
          <p><strong>Activo:</strong> ${this.user.active ? 'Si' : 'No'}</p>
          <p><strong>Email Verificado:</strong> ${this.user.emailVerified ? 'Si' : 'No'}</p>
          <p><strong>Genero:</strong> ${this.user.gender || 'N/A'}</p>
          <p><strong>Registrado:</strong> ${new Date(this.user.createdAt).toLocaleDateString()}</p>
          <p><strong>Actualizado:</strong> ${new Date(this.user.updatedAt).toLocaleDateString()}</p>
          
          <div class="btn-group" role="group">
            <a href="/users" class="btn btn-secondary">Volver</a>
            <button type="button" id="deleteBtn" class="btn btn-danger">Eliminar Usuario</button>
          </div>
        </div>
      </div>
    </div>
    
    <div class="col-md-4">
      <div class="card">
        <div class="card-header">
          <h5>Resumen</h5>
        </div>
        <div class="card-body">
          <p><strong>ID:</strong> ${this.user.id}</p>
          <p><strong>Nick:</strong> ${this.user.nick}</p>
          <p><strong>Rol:</strong> ${this.user.role}</p>
          <p><strong>Estado:</strong> ${this.user.active ? 'Activo' : 'Inactivo'}</p>
        </div>
      </div>
    </div>
  </div>
</div>
`;
    }

    setupListeners() {
        console.log('[LISTENERS] Configurando event listeners');
        const btn = document.getElementById('deleteBtn');
        if (btn) {
            btn.addEventListener('click', () => {
                console.log('[DELETE] Click en boton eliminar');
                if (confirm('Eliminar usuario ' + this.user.nick + '?')) {
                    this.delete();
                }
            });
        } else {
            console.warn('[LISTENERS] No se encontro boton deleteBtn');
        }
    }

    async delete() {
        console.log('[DELETE] Eliminando usuario:', this.user.id);
        try {
            const res = await UserService.deleteUser(this.user.id);
            if (res && res.success) {
                alert('Usuario eliminado exitosamente');
                App.getInstance().navigateTo('/users');
            } else {
                alert('Error al eliminar: ' + (res ? res.message : 'desconocido'));
            }
        } catch (e) {
            console.error('[DELETE ERROR]', e);
            alert('Error: ' + e.message);
        }
    }
}

console.log('[EXPORT] Intentando exportar UserDetailsComponent a window');
window.UserDetailsComponent = UserDetailsComponent;
console.log('[EXPORT] UserDetailsComponent asignado a window, tipo:', typeof window.UserDetailsComponent);

if (typeof AppScripts !== 'undefined') {
    console.log('[REGISTER] AppScripts disponible, registrando componente');
    AppScripts.register('user-details');
    console.log('[REGISTER] Componente registrado exitosamente');
} else {
    console.warn('[REGISTER] AppScripts no disponible en window');
}

console.log('[LOAD] user-details.component.js carga completada');
