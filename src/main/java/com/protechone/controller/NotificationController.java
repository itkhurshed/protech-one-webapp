package com.protechone.controller;

import com.protechone.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public String list(Model model) {
        model.addAttribute("notifications", notificationService.list(PageRequest.of(0, 50)).getContent());
        return "settings/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        notificationService.markRead(id);
        return "redirect:/notifications";
    }
}
