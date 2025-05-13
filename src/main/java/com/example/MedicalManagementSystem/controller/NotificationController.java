package com.example.MedicalManagementSystem.controller;

import com.example.MedicalManagementSystem.model.Notification;
import com.example.MedicalManagementSystem.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String getAllNotifications(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getUserIdFromUsername(auth.getName());

        List<Notification> notifications = notificationService.getAllNotificationsForUser(userId);
        model.addAttribute("notifications", notifications);

        return "notifications/list";
    }

    @PostMapping("/mark-read/{id}")
    @ResponseBody
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PostMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<String> markAllAsRead() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getUserIdFromUsername(auth.getName());

        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }

    @PostMapping("/generate-sample")
    @ResponseBody
    public ResponseEntity<String> generateSampleNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getUserIdFromUsername(auth.getName());

        notificationService.createSampleNotifications(userId);
        return ResponseEntity.ok("Sample notifications created");
    }

    // Helper method to get user ID from username
    // In a real application, you would look up the user ID from the database
    private Long getUserIdFromUsername(String username) {
        // For simplicity, we'll return a fixed ID
        // In a real application, you would query your user repository
        return 1L;
    }
}

