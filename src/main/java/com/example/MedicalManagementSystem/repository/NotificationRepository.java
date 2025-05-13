package com.example.MedicalManagementSystem.repository;

import com.example.MedicalManagementSystem.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find all notifications for a specific user
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find unread notifications for a specific user
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, boolean isRead);

    // Count unread notifications for a specific user
    long countByUserIdAndIsRead(Long userId, boolean isRead);

    // Mark all notifications as read for a specific user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadForUser(Long userId);
}

