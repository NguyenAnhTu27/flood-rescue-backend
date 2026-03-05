package com.swp391.rescuemanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Thêm origin của frontend vào đây
                .allowedOrigins(
                        "http://localhost:3000", // React CRA
                        "http://localhost:5173", // Vite
                        "http://localhost:4200" // Angular
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // BẮT BUỘC cho Session Cookie
                .maxAge(3600);
    }
}
