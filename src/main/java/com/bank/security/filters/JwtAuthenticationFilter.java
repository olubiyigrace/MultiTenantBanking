package com.bank.security.filters;

import com.bank.exceptions.InvalidRequestException;
import com.bank.security.JwtService;
import com.bank.services.RedisSessionService;
import com.bank.utils.InstitutionContext;
import com.bank.exceptions.UnauthorizedException;
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
    private final RedisSessionService redisSessionService;



    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        if (path.equals("/api/v1/auth/reset-password")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt)) {
                jwtService.validateToken(jwt);

                if (!jwtService.isAccessToken(jwt)) {
                    throw new UnauthorizedException("Invalid access token");
                }

                String sessionId = jwtService.getSessionId(jwt);
                if (!redisSessionService.isSessionActive(sessionId)) {
                    throw new UnauthorizedException("Session expired");
                }
                    final String userId = jwtService.getUserIdFromToken(jwt);
                    final String institutionId = jwtService.getInstitutionIdFromToken(jwt);
                    final String userAccountType = jwtService.getUserAccountTypeFromToken(jwt);

                    log.info("User Account Type: {}", userAccountType);

                    if (institutionId != null) {
                        InstitutionContext.setCurrentInstitution(institutionId);
                    }
                    if (userAccountType == null || userAccountType.isBlank()) {
                        log.warn("Missing userAccountType in JWT for userId={}", userId);
                        throw new UnauthorizedException("Missing role in JWT");
                    }
                    final SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userAccountType);
                    final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(authority)
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug(
                            "User authenticated for user ID:{}, institution: {}, role: {}",
                            userId,
                            institutionId,
                            userAccountType);
                }
                filterChain.doFilter(request, response);
            } catch(UnauthorizedException | InvalidRequestException ex){
                log.error("Error authenticating user", ex);
                SecurityContextHolder.clearContext();
                InstitutionContext.clear();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
            } finally{
                InstitutionContext.clear();
            }

    }

    private String getJwtFromRequest(final HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}