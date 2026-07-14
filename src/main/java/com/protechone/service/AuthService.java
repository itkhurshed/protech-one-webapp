package com.protechone.service;

import com.protechone.dto.auth.*;
import com.protechone.entity.*;
import com.protechone.exception.BadRequestException;
import com.protechone.exception.ResourceNotFoundException;
import com.protechone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Account lifecycle for the server-rendered app: self-service company
 * registration, forgot/reset password, change password, email verification.
 * Actual login/logout is handled declaratively by Spring Security's
 * formLogin (see SecurityConfig) — no manual authentication code needed
 * here. Login success/failure bookkeeping (lockout, login history) is
 * handled by AuthenticationEventListener.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("An account with this email already exists");
        }

        Company company = companyRepository.save(Company.builder()
                .name(request.companyName())
                .currencyCode("USD")
                .languageCode("en")
                .fiscalYearStartMonth(1)
                .isActive(true)
                .build());

        Branch branch = branchRepository.save(Branch.builder()
                .company(company)
                .name("Head Office")
                .code("HQ")
                .isMain(true)
                .isActive(true)
                .build());

        Role adminRole = roleRepository.findByCode("COMPANY_ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("Default role COMPANY_ADMIN not found. Did you run the DB migrations?"));

        return userRepository.save(User.builder()
                .company(company)
                .branch(branch)
                .role(adminRole)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .isActive(true)
                .isLocked(false)
                .failedLoginAttempts((short) 0)
                .mfaEnabled(false)
                .emailVerified(false)
                .emailVerifyToken(UUID.randomUUID().toString())
                .build());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(request.email()).ifPresent(user -> {
            user.setPasswordResetToken(UUID.randomUUID().toString());
            user.setPasswordResetExpires(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            // TODO: integrate with an email provider to deliver the reset link containing the token.
        });
        // Always behaves as successful to avoid leaking which emails are registered.
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.token())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
        if (user.getPasswordResetExpires() == null || user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired. Please request a new one.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        user.setIsLocked(false);
        user.setFailedLoginAttempts((short) 0);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerifyToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));
        user.setEmailVerified(true);
        user.setEmailVerifyToken(null);
        userRepository.save(user);
    }
}
