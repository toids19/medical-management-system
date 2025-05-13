package com.example.MedicalManagementSystem.repository;

import com.example.MedicalManagementSystem.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderAndRecipient(String sender, String recipient);
    List<ChatMessage> findByRoomId(Long roomId);
    List<ChatMessage> findByType(ChatMessage.MessageType type);
}
