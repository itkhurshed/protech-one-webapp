package com.protechone.service;

import com.protechone.dto.admin.RoleResponse;
import com.protechone.dto.admin.UserRequest;
import com.protechone.dto.admin.UserResponse;
import com.protechone.entity.Branch;
import com.protechone.entity.Role;
import com.protechone.entity.User;
import com.protechone.exception.BadRequestException;
import com.protechone.exception.ResourceNotFoundException;
import com.protechone.repository.BranchRepository;
import com.protechone.repository.RoleRepository;
import com.protechone.repository.UserRepository;
import com.protechone.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    public List<UserResponse> list() {
        return userRepository.findByCompanyId(currentUser.companyId()).stream().map(this::toResponse).toList();
    }

    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream()
                .map(r -> new RoleResponse(r.getId(), r.getCode(), r.getName(), r.getDescription()))
                .toList();
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("A user with this email already exists");
        }
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        Branch branch = request.branchId() != null ? branchRepository.findById(request.branchId()).orElse(null) : null;
        String rawPassword = (request.password() == null || request.password().isBlank())
                ? UUID.randomUUID().toString().substring(0, 12)
                : request.password();

        User user = userRepository.save(User.builder()
                .company(currentUser.get().getCompany())
                .branch(branch)
                .role(role)
                .employeeNumber(request.employeeNumber())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .isActive(request.isActive() == null || request.isActive())
                .isLocked(false)
                .failedLoginAttempts((short) 0)
                .mfaEnabled(false)
                .emailVerified(false)
                .build());

        return toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = findOwned(id);
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        Branch branch = request.branchId() != null ? branchRepository.findById(request.branchId()).orElse(null) : null;

        user.setEmployeeNumber(request.employeeNumber());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setRole(role);
        user.setBranch(branch);
        if (request.isActive() != null) user.setIsActive(request.isActive());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void toggleLock(Long id, boolean locked) {
        User user = findOwned(id);
        user.setIsLocked(locked);
        if (!locked) user.setFailedLoginAttempts((short) 0);
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.delete(findOwned(id));
    }

    private User findOwned(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        if (!user.getCompany().getId().equals(currentUser.companyId())) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        return user;
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getEmployeeNumber(), u.getFirstName(), u.getLastName(), u.getEmail(),
                u.getPhone(), u.getRole().getId(), u.getRole().getName(), u.getIsActive(), u.getIsLocked(),
                u.getMfaEnabled(), u.getLastLoginAt(), u.getCreatedAt());
    }
}
