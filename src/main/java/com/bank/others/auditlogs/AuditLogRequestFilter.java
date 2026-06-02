package com.bank.others.auditlogs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuditLogRequestFilter extends OncePerRequestFilter {
    public static final ThreadLocal<String> CLIENT_IP = new ThreadLocal<>();
    public static final ThreadLocal<String> ENDPOINT = new ThreadLocal<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }

        CLIENT_IP.set(ip);
        ENDPOINT.set(request.getMethod() + " " + request.getRequestURI());

        try {
            filterChain.doFilter(request, response);
        } finally {
            CLIENT_IP.remove();
            ENDPOINT.remove();
        }
    }
}
