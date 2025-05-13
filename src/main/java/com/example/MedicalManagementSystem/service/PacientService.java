package com.example.MedicalManagementSystem.service;

import com.example.MedicalManagementSystem.model.Pacient;
import com.example.MedicalManagementSystem.repository.PacientRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacientService {

    @Autowired
    private PacientRepository pacientRepository;
    
    public List<Pacient> getAllPatients() {
        return pacientRepository.findAll();
    }
    
    public Pacient getPatientById(Long id) {
        return pacientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
    }
    
    public Pacient createPatient(Pacient patient) {
        return pacientRepository.save(patient);
    }
    
    public Pacient updatePatient(Long id, Pacient patientDetails) {
        Pacient patient = getPatientById(id);
        patient.setNume(patientDetails.getNume());
        patient.setPrenume(patientDetails.getPrenume());
        
        return pacientRepository.save(patient);
    }
    
    public void deletePatient(Long id) {
        pacientRepository.deleteById(id);
    }
}

