package com.protechone.controller;

import com.protechone.dto.admin.UserRequest;
import com.protechone.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.list());
        return "settings/users-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new UserForm());
        model.addAttribute("isEdit", false);
        model.addAttribute("roles", userService.listRoles());
        return "settings/user-form";
    }

    @PostMapping
    public String create(@ModelAttribute UserForm form, RedirectAttributes redirectAttributes) {
        userService.create(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "User created successfully.");
        return "redirect:/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var user = userService.list().stream().filter(u -> u.id().equals(id)).findFirst().orElseThrow();
        UserForm form = new UserForm();
        form.setEmployeeNumber(user.employeeNumber()); form.setFirstName(user.firstName()); form.setLastName(user.lastName());
        form.setEmail(user.email()); form.setPhone(user.phone()); form.setRoleId(user.roleId()); form.setIsActive(user.isActive());
        model.addAttribute("form", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("userId", id);
        model.addAttribute("roles", userService.listRoles());
        return "settings/user-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute UserForm form, RedirectAttributes redirectAttributes) {
        userService.update(id, form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
        return "redirect:/users";
    }

    @PostMapping("/{id}/lock")
    public String lock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleLock(id, true);
        redirectAttributes.addFlashAttribute("successMessage", "User locked.");
        return "redirect:/users";
    }

    @PostMapping("/{id}/unlock")
    public String unlock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleLock(id, false);
        redirectAttributes.addFlashAttribute("successMessage", "User unlocked.");
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "User deleted.");
        return "redirect:/users";
    }

    @Data
    public static class UserForm {
        private String employeeNumber, firstName, lastName, email, phone, password;
        private Long roleId, branchId;
        private Boolean isActive = true;

        UserRequest toRequest() {
            return new UserRequest(employeeNumber, firstName, lastName, email, phone, password, roleId, branchId, isActive);
        }
    }
}
