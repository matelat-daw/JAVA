package com.futureprograms.MyIkea.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class SPAInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            String viewName = modelAndView.getViewName();
            
            // Si es una petición AJAX (SPA)
            if (isAjax(request)) {
                // Si la vista no es redirect, index o error, añadimos el header para que la SPA pueda actualizar el nav
                if (viewName != null && !viewName.startsWith("redirect:") && !viewName.equals("index") && !viewName.equals("error")) {
                    // El interceptor no cambia la vista, pero la SPA recibirá el HTML y extraerá el header si existe.
                    // Para que Spring renderice el header, podemos usar una técnica de fragmentos o simplemente 
                    // dejar que los controladores sigan devolviendo sus vistas y la SPA las maneje.
                }
                return;
            }

            // Si es una petición normal (no AJAX)
            // Si el nombre de la vista no empieza por redirect: y no es index ni error
            if (viewName != null && !viewName.startsWith("redirect:") && !viewName.equals("index")) {
                // Si la vista es "error", no la envolvemos en index para evitar recursion si index falla
                if (viewName.equals("error")) {
                    return;
                }
                modelAndView.addObject("view", viewName);
                modelAndView.setViewName("index");
            }
        }
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}