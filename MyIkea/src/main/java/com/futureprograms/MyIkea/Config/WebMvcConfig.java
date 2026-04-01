package com.futureprograms.MyIkea.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private SPAInterceptor spaInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        if (spaInterceptor != null) {
            registry.addInterceptor(spaInterceptor);
        }
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // ✅ Servir archivos estáticos desde la carpeta static
        registry
            .addResourceHandler("/images/**")
            .addResourceLocations("classpath:/static/images/")
            .setCachePeriod(3600); // 1 hora de cache

        registry
            .addResourceHandler("/css/**")
            .addResourceLocations("classpath:/static/css/")
            .setCachePeriod(3600);

        registry
            .addResourceHandler("/js/**")
            .addResourceLocations("classpath:/static/js/")
            .setCachePeriod(3600);

        // Permitir acceso a otros recursos estáticos
        registry
            .addResourceHandler("/**")
            .addResourceLocations("classpath:/static/");
    }
}
