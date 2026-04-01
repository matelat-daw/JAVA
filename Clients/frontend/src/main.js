/**
 * main.js - Punto de entrada de la aplicación
 * Inicializa la aplicación cuando todos los scripts se han cargado
 * Sistema de registro eficiente sin polling
 */

async function initializeApp() {
    try {
        console.log('🚀 Inicializando Clients API Frontend...');
        const startTime = performance.now();
        
        // Definir scripts requeridos
        const requiredScripts = ['constants', 'utils', 'client.model', 'client.service', 'auth.service', 'navbar', 'register', 'login', 'dashboard', 'profile', 'users', 'app'];
        window.AppScripts.required = requiredScripts;
        
        const maxWaitTime = 10000; // 10 segundos máximo
        const checkInterval = 50; // Revisar cada 50ms
        let timeWaited = 0;
        
        while (!AppScripts.allReady() && timeWaited < maxWaitTime) {
            // Mostrar progreso cada 1 segundo
            if (timeWaited % 1000 === 0 && timeWaited > 0) {
                const loaded = AppScripts.loaded.size;
                console.log(`⏳ Esperando scripts... (${loaded}/${requiredScripts.length})`);
            }
            
            await new Promise(resolve => setTimeout(resolve, checkInterval));
            timeWaited += checkInterval;
        }
        
        // Verificar que todos los scripts estén listos
        const missing = requiredScripts.filter(script => !AppScripts.isReady(script));
        if (missing.length > 0) {
            throw new Error(`Scripts faltantes: ${missing.join(', ')}`);
        }
        
        const loadTime = (performance.now() - startTime).toFixed(0);
        console.log(`✅ Todos los scripts cargados en ${loadTime}ms (${requiredScripts.length}/12)`);
        
        // Verificar que Bootstrap esté disponible
        if (!window.bootstrap) {
            throw new Error('Bootstrap no se pudo cargar desde CDN');
        }
        console.log('✅ Bootstrap disponible');
        
        // Inicializar la aplicación
        console.log('🎯 Inicializando aplicación...');
        App.init();
        console.log('✨ ¡Aplicación lista!');
        
    } catch (error) {
        console.error('❌ Error al inicializar:', error);
        const outlet = document.getElementById('router-outlet');
        if (outlet) {
            outlet.innerHTML = `
                <div class="container mt-5">
                    <div class="alert alert-danger" role="alert">
                        <h4 class="alert-heading">Error de inicialización</h4>
                        <p>${error.message}</p>
                        <hr>
                        <p class="mb-0 small">Por favor recarga la página. Si el problema persiste, verifica la consola del navegador.</p>
                        <button class="btn btn-primary mt-3" onclick="location.reload()">Recargar página</button>
                    </div>
                </div>
            `;
        }
    }
}

// Esperar a que el DOM esté listo antes de inicializar
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeApp);
} else {
    // El DOM ya está listo
    initializeApp();
}
