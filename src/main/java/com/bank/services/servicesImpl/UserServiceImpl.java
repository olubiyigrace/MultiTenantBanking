package com.bank.services.servicesImpl;

import com.bank.dtos.requestDtos.ChangePasswordRequest;
import com.bank.dtos.responseDtos.SessionResponse;
import com.bank.entities.User;
import com.bank.entities.UserSession;
import com.bank.exceptions.InvalidRequestException;
import com.bank.exceptions.UnauthorizedException;
import com.bank.repositories.UserRepository;
import com.bank.security.JwtService;
import com.bank.services.RedisSessionService;
import com.bank.services.UserService;
import com.bank.utils.CurrentUserUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    private final CurrentUserUtil currentUserUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RedisSessionService redisSessionService;


    @Override
    public void changePassword(ChangePasswordRequest request) {
        User loggedInUser = currentUserUtil.getLoggedInUser();

        boolean matches = passwordEncoder.matches(request.getOldPassword(), loggedInUser.getPassword());
        if (!matches) throw new InvalidRequestException("Old password is incorrect");

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Passwords do not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), loggedInUser.getPassword())) {
            throw new InvalidRequestException("Cannot reuse old password");
        }
        loggedInUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(loggedInUser);
        log.info("password changed successfully");
    }

    @Override
    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidRequestException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        jwtService.validateToken(token);
        if (!jwtService.isAccessToken(token)) {
            throw new InvalidRequestException("Invalid access token");
        }
        String sessionId = jwtService.getSessionId(token);
        redisSessionService.revokeSession(sessionId);
    }

    @Override
    public List<SessionResponse> getActiveSessions(HttpServletRequest request) {
        User currentUser = currentUserUtil.getLoggedInUser();
        String token = request.getHeader(HttpHeaders.AUTHORIZATION)
                .substring(7);
        String currentSessionId = jwtService.getSessionId(token);
        List<UserSession> sessions =
                redisSessionService.getUserSessions(currentUser.getId());
        return sessions.stream()
                .map(session -> SessionResponse.builder()
                        .sessionId(session.getSessionId())
                        .device(session.getUserAgent())
                        .ipAddress(session.getIpAddress())
                        .createdAt(session.getCreatedAt())
                        .expiresAt(session.getExpiresAt())
                        .currentSession(session.getSessionId().equals(currentSessionId))
                        .build())
                .toList();
    }

    @Override
    public void logoutAllDevices() {
        User user = currentUserUtil.getLoggedInUser();
        redisSessionService.revokeAllSessions(user.getId());
    }

    @Override
    public void revokeSession(String sessionId) {
        User currentUser = currentUserUtil.getLoggedInUser();
        UserSession session = redisSessionService.getSession(sessionId);
        if (session == null) {
            throw new InvalidRequestException("Session not found");
        }
        if (!session.getUserId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }
        redisSessionService.revokeSession(sessionId);
    }
}
