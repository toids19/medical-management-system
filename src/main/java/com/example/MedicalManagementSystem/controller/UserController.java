package com.example.MedicalManagementSystem.controller;

import com.example.MedicalManagementSystem.model.Medicament;
import com.example.MedicalManagementSystem.service.MedicamentService;
import com.example.MedicalManagementSystem.service.PacientService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private PacientService pacientService;

    @Autowired
    private MedicamentService medicamentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Add OCR link to dashboard
        model.addAttribute("showOcrFeature", true);
        model.addAttribute("title", "User Dashboard");
        model.addAttribute("header", "User Dashboard");
        return "user/dashboard";
    }

    @GetMapping("/pacienti")
    public String viewPatients(Model model) {
        model.addAttribute("patients", pacientService.getAllPatients());
        model.addAttribute("title", "Patients");
        model.addAttribute("header", "Patients");
        return "user/patients";
    }

    @GetMapping("/medicamente")
    public String viewMedications(Model model) {
        List<Medicament> medicamente = medicamentService.getAllMedications();

        // Create maps for medication categories and dosages
        Map<Long, String> medicationCategories = new HashMap<>();
        Map<Long, String> medicationDosages = new HashMap<>();


        model.addAttribute("medicamente", medicamente);
        model.addAttribute("medicationCategories", medicationCategories);
        model.addAttribute("medicationDosages", medicationDosages);
        model.addAttribute("title", "Medications");
        model.addAttribute("header", "Medications");

        return "user/medications";
    }
}
