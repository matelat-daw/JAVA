// SPA Router - Motor de navegación de la aplicación
// Maneja la carga dinámica de fragmentos y la navegación dentro de la aplicación

document.addEventListener('DOMContentLoaded', () => {
    const mainContent = document.getElementById('spa-content-target');
    const headerTarget = document.getElementById('spa-header-target');

    // Función para actualizar la SPA
    function updateSPA(html, url, pushState = true) {
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;
        
        // Buscar el nuevo header y el contenido real
        const newHeader = tempDiv.querySelector('header');
        const newContent = tempDiv.querySelector('.spa-content') || 
                         tempDiv.querySelector('.container') || 
                         tempDiv.querySelector('div > div') || 
                         tempDiv;

        if (newHeader) {
            headerTarget.innerHTML = newHeader.outerHTML;
        }
        
        mainContent.innerHTML = newContent.innerHTML;

        // Ejecutar scripts del fragmento si existen
        newContent.querySelectorAll('script').forEach(script => {
            const newScript = document.createElement('script');
            Array.from(script.attributes).forEach(attr => newScript.setAttribute(attr.name, attr.value));
            newScript.appendChild(document.createTextNode(script.innerHTML));
            document.body.appendChild(newScript).parentNode.removeChild(newScript);
        });
        
        // HOOK: Re-inicializar modales de autenticación después de cargar fragmento
        // Primero cerrar cualquier modal abierto y limpiar backdrops
        document.querySelectorAll('.modal').forEach(modal => {
            try {
                const instance = bootstrap.Modal.getInstance(modal);
                if (instance) {
                    instance.hide();
                }
            } catch (e) {
                console.log('[SPA-Router] Modal no tiene instancia');
            }
        });
        
        // Limpiar backdrops fantasma
        document.querySelectorAll('.modal-backdrop').forEach(backdrop => backdrop.remove());
        
        // Resetear flag de inicialización para que se ejecute de nuevo
        window.authModalsInitialized = false;
        document.body.dataset.profilePictureInitialized = 'false';
        
        setTimeout(() => {
            if (typeof window.initAuthModals === 'function') {
                console.log('[SPA-Router] Re-inicializando auth modals');
                window.initAuthModals();
            }
            
            if (typeof window.initProfilePictureUpload === 'function') {
                console.log('[SPA-Router] Re-inicializando profile picture upload');
                window.initProfilePictureUpload();
            }
        }, 300);
        
        if (pushState) {
            history.pushState({ url }, '', url);
        }
        
        bindLinks();
    }

    // Función para cargar contenido
    async function loadPage(url, pushState = true) {
        try {
            const response = await fetch(url, {
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            
            if (!response.ok) throw new Error('Error al cargar la página');
            
            const html = await response.text();
            updateSPA(html, response.url, pushState);
        } catch (error) {
            console.error('SPA Error:', error);
        }
    }

    // Función para vincular clics en enlaces
    function bindLinks() {
        document.querySelectorAll('a').forEach(link => {
            const href = link.getAttribute('href');
            if (href && href.startsWith('/') && !href.startsWith('/logout') && !link.getAttribute('target')) {
                link.onclick = (e) => {
                    e.preventDefault();
                    loadPage(href);
                };
            }
        });

        // Interceptar envíos de formularios
        document.querySelectorAll('form').forEach(form => {
            const action = form.getAttribute('action') || window.location.pathname;
            if (action && action.startsWith('/') && !action.startsWith('/logout') && !form.getAttribute('target')) {
                form.onsubmit = async (e) => {
                    e.preventDefault();
                    
                    const formData = new FormData(form);
                    const method = form.getAttribute('method') || 'GET';
                    
                    let url = action;
                    let options = {
                        method: method,
                        headers: {
                            'X-Requested-With': 'XMLHttpRequest'
                        }
                    };

                    if (method.toUpperCase() === 'GET') {
                        const params = new URLSearchParams(formData).toString();
                        url += (url.includes('?') ? '&' : '?') + params;
                    } else {
                        options.body = formData;
                    }

                    try {
                        const response = await fetch(url, options);
                        if (!response.ok) throw new Error('Error al enviar formulario');
                        
                        const html = await response.text();
                        updateSPA(html, response.url, true);
                    } catch (error) {
                        console.error('Form Error:', error);
                    }
                };
            }
        });
    }

    // Manejar navegación atrás/adelante del navegador
    window.onpopstate = (event) => {
        if (event.state && event.state.url) {
            loadPage(event.state.url, false);
        } else {
            location.reload();
        }
    };

    // Inicializar vínculos
    bindLinks();
});
