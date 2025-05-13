package com.example.MedicalManagementSystem.controller;

import com.example.MedicalManagementSystem.model.ChatMessage;
import com.example.MedicalManagementSystem.model.ChatRoom;
import com.example.MedicalManagementSystem.service.ChatGptService;
import com.example.MedicalManagementSystem.service.ChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatGptService chatGptService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        chatService.saveMessage(chatMessage);
        return chatMessage;
    }

    @MessageMapping("/chat.sendAnnouncement")
    @SendTo("/topic/announcements")
    public ChatMessage sendAnnouncement(@Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.ANNOUNCEMENT);
        chatService.saveMessage(chatMessage);
        return chatMessage;
    }

    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.PRIVATE);
        chatService.saveMessage(chatMessage);

        // Send to recipient
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipient(),
                "/queue/private",
                chatMessage
        );

        // Also send back to sender to update their view
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSender(),
                "/queue/private",
                chatMessage
        );
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        chatService.addUser(chatMessage.getSender());
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatService.saveMessage(chatMessage);

        // Broadcast updated user list
        messagingTemplate.convertAndSend("/topic/users", chatService.getActiveUsers());

        return chatMessage;
    }

    @MessageMapping("/chat.ai")
    public void processAiQuestion(@Payload ChatMessage chatMessage) {
        // Save the user question
        chatMessage.setType(ChatMessage.MessageType.AI_QUESTION);
        chatService.saveMessage(chatMessage);

        // Extract the actual question
        final String question = chatMessage.getContent();
        final String sender = chatMessage.getSender();

        // Log the question
        System.out.println("Received AI question from " + sender + ": " + question);

        // Process the AI question asynchronously
        CompletableFuture.runAsync(() -> {
            String response;
            try {
                // Try using OpenAI API
                System.out.println("Generating AI response...");
                response = chatGptService.generateResponse(question);
                System.out.println("AI response generated: " + response);
            } catch (Exception e) {
                // If API fails, use mock responses
                System.err.println("Error generating AI response: " + e.getMessage());
                e.printStackTrace();
                response = "Ne pare rău, am întâmpinat o problemă în generarea răspunsului. " +
                        "Vă rugăm să încercați din nou mai târziu. Eroare: " + e.getMessage();
            }

            ChatMessage responseMessage = new ChatMessage();
            responseMessage.setContent(response);
            responseMessage.setSender("AI Assistant");
            responseMessage.setType(ChatMessage.MessageType.AI_RESPONSE);
            responseMessage.setRecipient(sender); // Set the recipient to the original sender

            // Save the AI response
            chatService.saveMessage(responseMessage);

            System.out.println("Sending AI response to " + sender);

            // Send the response to the specific user
            messagingTemplate.convertAndSendToUser(
                    sender,
                    "/queue/ai-responses",
                    responseMessage
            );

            // Also send to public topic for debugging
            messagingTemplate.convertAndSend(
                    "/topic/ai-debug",
                    "AI response sent to " + sender
            );
        });
    }

    @GetMapping("/api/users/active")
    @ResponseBody
    public ResponseEntity<List<String>> getActiveUsers() {
        return ResponseEntity.ok(chatService.getActiveUsers());
    }

    @GetMapping("/api/chat/private/{recipient}")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getPrivateMessages(@PathVariable String recipient) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String sender = auth.getName();

        return ResponseEntity.ok(chatService.getPrivateMessages(sender, recipient));
    }

    @GetMapping("/api/chat/room/create/{otherUser}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createPrivateRoom(@PathVariable String otherUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        ChatRoom room = chatService.createPrivateRoom(currentUser, otherUser);

        Map<String, Object> response = new HashMap<>();
        response.put("roomId", room.getId());
        response.put("roomName", room.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/chat")
    public String getChat(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        model.addAttribute("username", username);
        model.addAttribute("recentMessages", chatService.getRecentMessages());
        model.addAttribute("activeUsers", chatService.getActiveUsers());
        model.addAttribute("title", "Chat System");
        model.addAttribute("header", "Chat System");

        return "chat/chat";
    }

    @GetMapping("/chat/standalone")
    public String getChatStandalone(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String role = auth.getAuthorities().stream().findFirst().get().getAuthority();

        model.addAttribute("username", username);
        model.addAttribute("userRole", role);
        model.addAttribute("recentMessages", chatService.getRecentMessages());
        model.addAttribute("activeUsers", chatService.getActiveUsers());

        return "chat/chat-standalone";
    }
}
