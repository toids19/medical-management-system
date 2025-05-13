package com.example.MedicalManagementSystem.repository;

import com.example.MedicalManagementSystem.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByName(String name);

    List<ChatRoom> findByParticipantsContaining(String username);

    @Query("SELECT r FROM ChatRoom r WHERE :user1 MEMBER OF r.participants AND :user2 MEMBER OF r.participants AND r.isPrivate = true")
    Optional<ChatRoom> findByParticipantsContainingAndParticipantsContaining(
            @Param("user1") String user1,
            @Param("user2") String user2
    );
}
