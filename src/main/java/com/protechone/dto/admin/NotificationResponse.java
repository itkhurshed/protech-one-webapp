package com.protechone.dto.admin;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, String title, String message, String type, String link, Boolean isRead, LocalDateTime createdAt) {}
