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
public final class SwaggerUiUrlLogger {

    private static final Logger log = getLogger(SwaggerUiUrlLogger.class);

    private final SwaggerUiConfigProperties properties;
    private String path;
    private Integer port;

    public SwaggerUiUrlLogger(SwaggerUiConfigProperties properties) {
        this.path = null;
        this.port = null;
        this.properties = requireNonNull(properties);
    }

    @PostConstruct
    public void readPath() {
        path = properties.getPath();
        logSwaggerUiUrl();
    }

    @EventListener
    public void onApplicationEvent(WebServerInitializedEvent event) {
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

