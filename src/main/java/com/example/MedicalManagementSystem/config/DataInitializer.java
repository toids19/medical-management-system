package com.example.MedicalManagementSystem.config;

import com.example.MedicalManagementSystem.model.Notification;
import com.example.MedicalManagementSystem.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.logging.Logger;

@Configuration
public class DataInitializer {

    private static final Logger logger = Logger.getLogger(DataInitializer.class.getName());

    @Bean
    @Order(3) // Execute after other initializers
    public CommandLineRunner initNotifications(NotificationService notificationService) {
        return args -> {
            logger.info("Initializing sample notifications...");

            // Create sample notifications for user ID 1 (adjust if your user IDs are different)
            Long userId = 1L;

            // Create a few sample notifications
            notificationService.createNotification(
                    "Welcome to the Medical Management System!",
                    Notification.NotificationType.TASK,
                    userId
            );

            notificationService.createNotification(
                    "Patient John Doe has an appointment tomorrow at 10:00 AM",
                    Notification.NotificationType.APPOINTMENT,
                    userId,
                    1L,
                    "/user/appointments/1"
            );

            notificationService.createNotification(
                    "Ibuprofen stock is running low (5 units remaining)",
                    Notification.NotificationType.MEDICATION_STOCK,
                    userId,
                    2L,
                    "/user/medicamente"
            );

            notificationService.createNotification(
                    "You have a new task: Update patient records",
                    Notification.NotificationType.TASK,
                    userId,
                    3L,
                    "/user/tasks/3"
            );

            logger.info("Sample notifications created successfully");
        };
    }
}

