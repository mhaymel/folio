package com.folio.config;

import static org.slf4j.LoggerFactory.getLogger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public final class RequestLoggingFilter implements Filter {

    private static final Logger log = getLogger(RequestLoggingFilter.class);


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        log.info("Incoming request: {} {}", req.getMethod(), req.getRequestURI());
        chain.doFilter(request, response);
    }
}

