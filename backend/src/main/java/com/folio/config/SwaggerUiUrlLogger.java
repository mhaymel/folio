package com.folio.config;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
final class SwaggerUiUrlLogger {

    private static final Logger log = getLogger(SwaggerUiUrlLogger.class);

    private final SwaggerUiConfigProperties properties;
    private String path;
    private Integer port;

    private SwaggerUiUrlLogger(SwaggerUiConfigProperties properties, String path, Integer port) {
        this.properties = requireNonNull(properties);
        this.path = path;
        this.port = port;
    }

    SwaggerUiUrlLogger(SwaggerUiConfigProperties properties) {
        this(properties, null, null);
    }

    @PostConstruct
    void readPath() {
        path = properties.getPath();
        logSwaggerUiUrl();
    }

    @EventListener
    void onApplicationEvent(WebServerInitializedEvent event) {
        port = event.getWebServer().getPort();
        logSwaggerUiUrl();
    }

    private void logSwaggerUiUrl() {
        if (isNull(path) || isNull(port)) {
            return;
        }
        log.info("swagger-ui can be found here: http://localhost:{}{}", port, path);
    }
}
