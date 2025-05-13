package com.example.MedicalManagementSystem.repository;

import com.example.MedicalManagementSystem.model.Medicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicamentRepository extends JpaRepository<Medicament, Long> {
}

