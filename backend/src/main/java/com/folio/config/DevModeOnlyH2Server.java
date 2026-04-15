package com.folio.config;

import static org.slf4j.LoggerFactory.getLogger;

import jakarta.annotation.PostConstruct;
import java.sql.SQLException;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
final class DevModeOnlyH2Server {

    private static final Logger LOG = getLogger(DevModeOnlyH2Server.class);
    private static final String ORG_H2_DRIVER = "org.h2.Driver";

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @PostConstruct
    public void init() {
        LOG.info("\"dev\" mode is active");
        LOG.info("spring.datasource.driver-class-name: {}", driverClassName);
        if (isH2Database()) {
            startH2Server();
        }
    }

    private void startH2Server() {
        LOG.info("Starting H2 server ...");
        try {
            Server server = Server.createTcpServer("-tcpAllowOthers").start();
            LOG.info("H2 server started. Connection is available at: {}", server.getURL());
            LOG.info("The IP address can probably be replaced by \"localhost\" e.g. tcp://localhost:9092");
        } catch (SQLException exception) {
            LOG.error("Could not start H2 server!", exception);
        }
    }

    private boolean isH2Database() {
        return ORG_H2_DRIVER.equalsIgnoreCase(driverClassName);
    }
}

