package com.protechone.controller;

import com.protechone.entity.User;
import com.protechone.repository.NotificationRepository;
import com.protechone.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injects data every Thymeleaf template needs regardless of which
 * controller served the page: the signed-in user, their unread
 * notification count, and the light/dark + LTR/RTL preferences (read from
 * plain cookies set by PreferencesController — no JavaScript involved).
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @ModelAttribute("currentUser")
    public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(auth.getName()).orElse(null);
    }

    @ModelAttribute("unreadNotifications")
    public long unreadNotifications() {
        User user = currentUser();
        return user == null ? 0 : notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @ModelAttribute("theme")
    public String theme(HttpServletRequest request) {
        return cookieValue(request, "protech_theme", "light");
    }

    @ModelAttribute("dir")
    public String dir(HttpServletRequest request) {
        return cookieValue(request, "protech_dir", "ltr");
    }

    private String cookieValue(HttpServletRequest request, String name, String fallback) {
        if (request.getCookies() == null) return fallback;
        for (Cookie c : request.getCookies()) {
            if (c.getName().equals(name)) return c.getValue();
        }
        return fallback;
    }
}
