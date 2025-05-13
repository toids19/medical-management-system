package com.example.MedicalManagementSystem.repository;

import com.example.MedicalManagementSystem.model.Pacient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacientRepository extends JpaRepository<Pacient, Long> {
}

