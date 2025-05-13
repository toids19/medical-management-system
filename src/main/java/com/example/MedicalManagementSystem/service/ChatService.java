package com.example.MedicalManagementSystem.service;

import com.example.MedicalManagementSystem.model.ChatMessage;
import com.example.MedicalManagementSystem.model.ChatRoom;
import com.example.MedicalManagementSystem.repository.ChatMessageRepository;
import com.example.MedicalManagementSystem.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    // Store active users
    private Set<String> activeUsers = ConcurrentHashMap.newKeySet();

    // Store session messages (cleared on application restart)
    private List<ChatMessage> sessionMessages = Collections.synchronizedList(new ArrayList<>());

    @EventListener(ContextRefreshedEvent.class)
    public void clearMessagesOnStartup(ContextRefreshedEvent event) {
        // Clear session messages when application starts
        sessionMessages.clear();
        System.out.println("Chat messages cleared on application startup");
    }

    public void addUser(String username) {
        activeUsers.add(username);
    }

    public void removeUser(String username) {
        activeUsers.remove(username);
    }

    public List<String> getActiveUsers() {
        return new ArrayList<>(activeUsers);
    }

    public ChatMessage saveMessage(ChatMessage message) {
        // Set timestamp if not already set
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }

        // Save to session storage
        sessionMessages.add(message);

        // Save to database for persistence if needed
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getRecentMessages() {
        // Return the most recent messages (last 50)
        int maxSize = 50;
        return sessionMessages.stream()
                .filter(msg -> msg.getType() == ChatMessage.MessageType.CHAT ||
                        msg.getType() == ChatMessage.MessageType.JOIN ||
                        msg.getType() == ChatMessage.MessageType.LEAVE)
                .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                .limit(maxSize)
                .collect(Collectors.toList());
    }

    public List<ChatMessage> getPrivateMessages(String sender, String recipient) {
        // Return private messages between two users
        return sessionMessages.stream()
                .filter(msg -> msg.getType() == ChatMessage.MessageType.PRIVATE)
                .filter(msg ->
                        (msg.getSender().equals(sender) && msg.getRecipient().equals(recipient)) ||
                                (msg.getSender().equals(recipient) && msg.getRecipient().equals(sender))
                )
                .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * Find an existing private room between two users or create a new one
     * @param user1 First user
     * @param user2 Second user
     * @return Optional containing the room if found or created successfully, empty otherwise
     */
    public Optional<ChatRoom> findOrCreatePrivateRoom(String user1, String user2) {
        try {
            // Check if room already exists
            Optional<ChatRoom> existingRoom = chatRoomRepository.findByParticipantsContainingAndParticipantsContaining(user1, user2);

            if (existingRoom.isPresent()) {
                return existingRoom;
            }

            // Create new room
            ChatRoom room = new ChatRoom();
            room.setName(user1 + "-" + user2);
            room.setPrivate(true);
            room.setCreatedBy(user1);

            // Add participants
            Set<String> participants = new HashSet<>();
            participants.add(user1);
            participants.add(user2);
            room.setParticipants(participants);

            // Save and return the new room
            return Optional.of(chatRoomRepository.save(room));
        } catch (Exception e) {
            System.err.println("Error creating private room: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public ChatRoom createPrivateRoom(String user1, String user2) {
        // This method is kept for backward compatibility
        return findOrCreatePrivateRoom(user1, user2).orElseThrow(() ->
                new RuntimeException("Failed to create private room between " + user1 + " and " + user2));
    }

    public String generateMockAiResponse(String question) {
        // Simple mock AI responses for testing
        question = question.toLowerCase();

        if (question.contains("hello") || question.contains("hi ")) {
            return "Hello! How can I assist you with the Medical Management System today?";
        } else if (question.contains("medication") || question.contains("medicine")) {
            return "The system allows you to manage medications, including adding new medications, updating dosage information, and tracking inventory.";
        } else if (question.contains("patient")) {
            return "You can manage patient records, view medical history, and schedule appointments through the patient management module.";
        } else if (question.contains("appointment") || question.contains("schedule")) {
            return "To schedule an appointment, navigate to the patient's profile and click on 'Schedule Appointment'. You can then select an available time slot.";
        } else if (question.contains("report") || question.contains("analytics")) {
            return "The reporting module provides analytics on patient visits, medication usage, and other key metrics. You can export these reports as PDF or CSV.";
        } else {
            return "I'm not sure how to help with that specific query. Could you provide more details or ask about a different aspect of the Medical Management System?";
        }
    }
}
