package com.example.MedicalManagementSystem.service;

import com.example.MedicalManagementSystem.model.Medicament;
import com.example.MedicalManagementSystem.repository.MedicamentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicamentService {

    @Autowired
    private MedicamentRepository medicamentRepository;
    
    public List<Medicament> getAllMedications() {
        return medicamentRepository.findAll();
    }
    
    public Medicament getMedicationById(Long id) {
        return medicamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));
    }
    
    public Medicament createMedication(Medicament medication) {
        return medicamentRepository.save(medication);
    }
    
    public Medicament updateMedication(Long id, Medicament medicationDetails) {
        Medicament medication = getMedicationById(id);
        medication.setNume(medicationDetails.getNume());
        
        return medicamentRepository.save(medication);
    }
    
    public void deleteMedication(Long id) {
        medicamentRepository.deleteById(id);
    }
}

