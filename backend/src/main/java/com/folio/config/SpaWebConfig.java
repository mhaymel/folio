package com.folio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * SPA fallback: all non-API, non-static routes serve index.html
 * so that React Router can handle client-side routing.
 */
@Configuration
class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location) throws IOException {
                    Resource requested = location.createRelative(resourcePath);
                    // Serve the actual file if it exists, otherwise fall back to index.html
                    return requested.exists() && requested.isReadable()
                        ? requested
                        : new ClassPathResource("/static/index.html");
                }
            });
    }
}

