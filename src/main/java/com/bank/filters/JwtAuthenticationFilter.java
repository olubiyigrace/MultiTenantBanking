//package com.bank.filters;
//
//import com.bank.services.JwtService;
//import jakarta.persistence.EntityNotFoundException;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import tools.jackson.databind.ObjectMapper;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.Map;
//
//@RequiredArgsConstructor
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//    private final JwtService jwtService;
//    private final UserDetailsService userDetailsService;
//
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        final String authHeader = request.getHeader("Authorization");
//        final String jwt;
//        final String username;
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        jwt = getJwtFromRequest(request);
//        if (!jwtService.isValidToken(jwt)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        try {
//            username = jwtService.extractUsernameFromToken(jwt);
//            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//                if (jwtService.validateTokenForUser(jwt, userDetails)) {
//                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//                }
//            }
//            filterChain.doFilter(request, response);
//        } catch (EntityNotFoundException entityNotFoundException) {
//            sendErrorResponse(response, "Unauthorized", HttpStatus.UNAUTHORIZED);
//        } catch (Exception exception) {
//            sendErrorResponse(response, "Authentication error; " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//    private String getJwtFromRequest (HttpServletRequest request){
//        final String authHeader = request.getHeader("Authorization");
//        return authHeader.substring(7);
//    }
//
//    private void sendErrorResponse (HttpServletResponse response, String message, HttpStatus status) throws
//            IOException {
//        response.setStatus(status.value());
//        response.setContentType("application/json");
//        Map<String, Object> body = Map.of(
//                "timestamp", LocalDateTime.now().toString(),
//                "status", status.value(),
//                "error", message
//        );
//        new ObjectMapper().writeValue(response.getWriter(), body);
//    }
//}
