package com.hotelbooking.hotel_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceLocation;
        
        if (uploadDir.startsWith("/app")) {
            resourceLocation = "file:" + uploadDir + "/";
        } else {
            resourceLocation = "file:./" + uploadDir + "/";
        }
        
        System.out.println("Serving static files from: " + resourceLocation);
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}