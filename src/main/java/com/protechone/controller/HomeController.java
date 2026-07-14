package com.protechone.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
        return authenticated ? "home" : "home";
        // Home page itself adapts its call-to-action based on ${currentUser} in the template.
    }
}
