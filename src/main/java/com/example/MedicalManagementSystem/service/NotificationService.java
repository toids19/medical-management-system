package com.example.MedicalManagementSystem.service;

import com.example.MedicalManagementSystem.model.Notification;
import com.example.MedicalManagementSystem.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(String message, Notification.NotificationType type, Long userId) {
        Notification notification = new Notification(message, type, userId);
        return notificationRepository.save(notification);
    }

    public Notification createNotification(String message, Notification.NotificationType type, Long userId, Long referenceId, String link) {
        Notification notification = new Notification(message, type, userId, referenceId, link);
        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }

    public long countUnreadNotificationsForUser(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }

    // For testing purposes - create sample notifications
    public void createSampleNotifications(Long userId) {
        createNotification(
                "Patient John Doe has an appointment tomorrow at 10:00 AM",
                Notification.NotificationType.APPOINTMENT,
                userId,
                1L,
                "/user/appointments/1"
        );

        createNotification(
                "Ibuprofen stock is running low (5 units remaining)",
                Notification.NotificationType.MEDICATION_STOCK,
                userId,
                2L,
                "/user/medicamente"
        );

        createNotification(
                "You have a new task: Update patient records",
                Notification.NotificationType.TASK,
                userId,
                3L,
                "/user/tasks/3"
        );
    }
}

