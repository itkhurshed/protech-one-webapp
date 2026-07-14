package com.protechone.security;

import com.protechone.entity.LoginHistory;
import com.protechone.entity.User;
import com.protechone.repository.LoginHistoryRepository;
import com.protechone.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Hooks into Spring Security's authentication events to implement account
 * lockout after repeated failures and to persist a login_history audit
 * trail — replicated here from the JWT edition's manual bookkeeping, now
 * driven by the framework's own event publishing instead of custom code
 * inside a login endpoint.
 */
@Component
@RequiredArgsConstructor
public class AuthenticationEventListener {

    private static final int MAX_LOGIN_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    @org.springframework.context.event.EventListener
    @Transactional
    public void onSuccess(AuthenticationSuccessEvent event) {
        String email = event.getAuthentication().getName();
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            user.setFailedLoginAttempts((short) 0);
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            recordLogin(user, email, true, "SUCCESS");
        });
    }

    @org.springframework.context.event.EventListener
    @Transactional
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String email = String.valueOf(event.getAuthentication().getPrincipal());
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            user.setFailedLoginAttempts((short) (user.getFailedLoginAttempts() + 1));
            if (user.getFailedLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
                user.setIsLocked(true);
            }
            userRepository.save(user);
            recordLogin(user, email, false, "BAD_CREDENTIALS");
        });
    }

    private void recordLogin(User user, String emailTried, boolean success, String reason) {
        String ip = null;
        String agent = null;
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            ip = req.getRemoteAddr();
            agent = req.getHeader("User-Agent");
        }
        loginHistoryRepository.save(LoginHistory.builder()
                .user(user)
                .emailTried(emailTried)
                .ipAddress(ip)
                .userAgent(agent)
                .success(success)
                .reason(reason)
                .build());
    }
}
