package com.organica.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
//public class CorsConfig implements WebMvcConfigurer{
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        // TODO Auto-generated method stub
//                registry.addMapping("/**")
//            .allowedOrigins("*")
//            .allowedMethods("*")
//            .allowedHeaders("*")
//            .allowCredentials(true);
//
//        WebMvcConfigurer.super.addCorsMappings(registry);
//    }
//
//}

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000") // Add your frontend URL here
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

