package com.example.MedicalManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private Long userId;

    // Optional: reference ID (e.g., patient ID, medication ID)
    private Long referenceId;

    // Optional: link to navigate to when clicked
    private String link;

    public enum NotificationType {
        APPOINTMENT,
        MEDICATION_STOCK,
        TASK
    }

    // Constructor for easy creation
    public Notification(String message, NotificationType type, Long userId) {
        this.message = message;
        this.type = type;
        this.userId = userId;
    }

    // Constructor with reference ID and link
    public Notification(String message, NotificationType type, Long userId, Long referenceId, String link) {
        this.message = message;
        this.type = type;
        this.userId = userId;
        this.referenceId = referenceId;
        this.link = link;
    }
}

