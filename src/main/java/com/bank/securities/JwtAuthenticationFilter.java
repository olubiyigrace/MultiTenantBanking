package com.bank.securities;

import com.bank.config.InstitutionContext;
import com.bank.config.InstitutionSchemaResolver;
import com.bank.exceptions.UnauthorizedException;
import com.bank.auth.logout.LogoutTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final InstitutionSchemaResolver institutionSchemaResolver;
    private final LogoutTokenRepository logoutTokenRepository;

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().contains("/api/v1/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            final String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && logoutTokenRepository.existsById(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("User logged out");
                return;
            }
            if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt)) {

                final String userId = jwtService.getUserIdFromToken(jwt);
                final String institutionId = jwtService.getInstitutionIdFromToken(jwt);
                final String userAccountType = jwtService.getUserAccountTypeFromToken(jwt);

                log.info("User Account Type: {}", userAccountType);

                if (institutionId != null) {
                    InstitutionContext.setCurrentInstitution(institutionId);

                    final String schemaName =
                            institutionSchemaResolver.resolveInstitutionSchema(institutionId);

                    InstitutionContext.setCurrentSchema(schemaName);
                }
                if (userAccountType == null || userAccountType.isBlank()) {
                    log.warn("Missing userAccountType in JWT for userId={}", userId);
                    throw new UnauthorizedException("Missing role in JWT");
                }
                final SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority("ROLE_" + userAccountType);
                final UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.singletonList(authority)
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug(
                        "User authenticated for user ID:{}, institution: {}, role: {}",
                        userId,
                        institutionId,
                        userAccountType);
            }
        } catch (final Exception e) {
            log.error("Error authenticating user", e);
        }
        filterChain.doFilter(request, response);
        InstitutionContext.clear();
    }

    private String getJwtFromRequest(final HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
