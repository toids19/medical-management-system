package com.example.MedicalManagementSystem.controller;

import com.example.MedicalManagementSystem.model.Notification;
import com.example.MedicalManagementSystem.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final NotificationService notificationService;

    @Autowired
    public GlobalControllerAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("notifications")
    public List<Notification> getNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            Long userId = getUserIdFromUsername(auth.getName());
            return notificationService.getUnreadNotificationsForUser(userId);
        }
        return Collections.emptyList();
    }

    @ModelAttribute("unreadCount")
    public long getUnreadCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            Long userId = getUserIdFromUsername(auth.getName());
            return notificationService.countUnreadNotificationsForUser(userId);
        }
        return 0;
    }

    // Helper method to get user ID from username
    // In a real application, you would look up the user ID from the database
    private Long getUserIdFromUsername(String username) {
        // For simplicity, we'll return a fixed ID
        // In a real application, you would query your user repository
        return 1L;
    }
}

