package com.sispro3d.unam.core.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Sirve los archivos guardados por {@link FileStorageService} bajo {@code /files/**}.
 */
@Configuration
public class StorageWebConfig implements WebMvcConfigurer {

    private final String location;

    public StorageWebConfig(@Value("${app.storage.location:uploads}") String location) {
        this.location = location;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String base = Paths.get(location).toAbsolutePath().normalize().toUri().toString();
        if (!base.endsWith("/")) {
            base = base + "/";
        }
        registry.addResourceHandler("/files/**").addResourceLocations(base);
    }
}
