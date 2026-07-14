package com.protechone.controller;

import com.protechone.dto.auth.*;
import com.protechone.entity.User;
import com.protechone.exception.BadRequestException;
import com.protechone.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Plain HTML forms for everything Spring Security's formLogin doesn't cover
 * itself: the login page's markup (POST is handled declaratively by
 * SecurityConfig), self-service company registration, and forgot/reset
 * password. Every POST here is a normal form submit + redirect — no fetch(),
 * no JSON.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                             @RequestParam(required = false) String logout,
                             @RequestParam(required = false) String expired,
                             @RequestParam(required = false) String registered,
                             Model model) {
        if (error != null) model.addAttribute("errorMessage", "Invalid email or password.");
        if (logout != null) model.addAttribute("successMessage", "You have been signed out.");
        if (expired != null) model.addAttribute("errorMessage", "Your session expired. Please sign in again.");
        if (registered != null) model.addAttribute("successMessage", "Workspace created! Sign in with your new account.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterFormData());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterFormData form, Model model, RedirectAttributes redirectAttributes) {
        try {
            RegisterRequest request = new RegisterRequest(form.getCompanyName(), form.getFirstName(),
                    form.getLastName(), form.getEmail(), form.getPassword());
            User user = authService.register(request);
            redirectAttributes.addAttribute("registered", "true");
            return "redirect:/login";
        } catch (BadRequestException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("registerRequest", form);
            return "auth/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        authService.forgotPassword(new ForgotPasswordRequest(email));
        model.addAttribute("successMessage", "If that email is registered, a reset link has been sent.");
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String newPassword, Model model) {
        try {
            authService.resetPassword(new ResetPasswordRequest(token, newPassword));
            return "redirect:/login";
        } catch (BadRequestException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
    }

    /** Plain form-binding holder for the registration page (kept separate from the record-based RegisterRequest DTO). */
    public static class RegisterFormData {
        private String companyName, firstName, lastName, email, password;
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String v) { this.companyName = v; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String v) { this.firstName = v; }
        public String getLastName() { return lastName; }
        public void setLastName(String v) { this.lastName = v; }
        public String getEmail() { return email; }
        public void setEmail(String v) { this.email = v; }
        public String getPassword() { return password; }
        public void setPassword(String v) { this.password = v; }
    }
}
