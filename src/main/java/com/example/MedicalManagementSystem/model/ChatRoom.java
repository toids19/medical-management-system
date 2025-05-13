package com.example.MedicalManagementSystem.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String createdBy;

    private boolean isPrivate;

    @ElementCollection
    @CollectionTable(name = "chat_room_participants", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "participant")
    private Set<String> participants = new HashSet<>();

    public enum RoomType {
        PUBLIC,
        PRIVATE,
        GROUP
    }

    @Enumerated(EnumType.STRING)
    private RoomType type;

    // Default constructor
    public ChatRoom() {
        this.participants = new HashSet<>();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<String> participants) {
        this.participants = participants;
    }

    public void addParticipant(String participant) {
        if (this.participants == null) {
            this.participants = new HashSet<>();
        }
        this.participants.add(participant);
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }
}
