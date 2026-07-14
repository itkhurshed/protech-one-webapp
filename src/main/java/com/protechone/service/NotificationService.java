package com.protechone.service;

import com.protechone.dto.admin.NotificationResponse;
import com.protechone.entity.Notification;
import com.protechone.repository.NotificationRepository;
import com.protechone.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUser currentUser;

    public Page<NotificationResponse> list(Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.get().getId(), pageable)
                .map(n -> new NotificationResponse(n.getId(), n.getTitle(), n.getMessage(), n.getType(), n.getLink(), n.getIsRead(), n.getCreatedAt()));
    }

    public long unreadCount() {
        return notificationRepository.countByUserIdAndIsReadFalse(currentUser.get().getId());
    }

    @Transactional
    public void markRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public Notification push(com.protechone.entity.Company company, com.protechone.entity.User user, String title, String message, String type, String link) {
        return notificationRepository.save(Notification.builder()
                .company(company).user(user).title(title).message(message)
                .type(type == null ? "INFO" : type).link(link).isRead(false).build());
    }
}
