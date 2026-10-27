package com.tienda.zero.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapea /assets/** buscando en las nuevas subcarpetas static/tienda/ y static/admin/
        registry.addResourceHandler("/assets/**")
                .addResourceLocations(
                        "classpath:/static/tienda/",
                        "classpath:/static/admin/",
                        "classpath:/static/"
                );

        // Rutas específicas para tienda y admin
        registry.addResourceHandler("/shop/**")
                .addResourceLocations("classpath:/static/tienda/");

        registry.addResourceHandler("/admin/**")
                .addResourceLocations("classpath:/static/admin/");

        registry.addResourceHandler("/admin/assets/**")
                .addResourceLocations("classpath:/static/admin/");
    }
}
