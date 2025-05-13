package com.example.MedicalManagementSystem.controller;

import com.example.MedicalManagementSystem.model.Medicament;
import com.example.MedicalManagementSystem.model.Pacient;
import com.example.MedicalManagementSystem.model.User;
import com.example.MedicalManagementSystem.service.MedicamentService;
import com.example.MedicalManagementSystem.service.PacientService;
import com.example.MedicalManagementSystem.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PacientService pacientService;
    
    @Autowired
    private MedicamentService medicamentService;
    
    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
    
    // User Management
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }
    
    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/edit_user";
    }
    
    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id, User user) {
        userService.updateUser(id, user);
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/add")
    public String addUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/add_user";
    }
    
    @PostMapping("/users/add")
    public String addUser(User user) {
        userService.createUser(user);
        return "redirect:/admin/users";
    }
    
    // Patient Management
    @GetMapping("/pacienti")
    public String listPatients(Model model) {
        model.addAttribute("patients", pacientService.getAllPatients());
        return "admin/patients";
    }
    
    @GetMapping("/pacienti/edit/{id}")
    public String editPatientForm(@PathVariable Long id, Model model) {
        Pacient patient = pacientService.getPatientById(id);
        model.addAttribute("patient", patient);
        return "admin/edit_patient";
    }
    
    @PostMapping("/pacienti/edit/{id}")
    public String updatePatient(@PathVariable Long id, Pacient patient) {
        pacientService.updatePatient(id, patient);
        return "redirect:/admin/pacienti";
    }
    
    @GetMapping("/pacienti/delete/{id}")
    public String deletePatient(@PathVariable Long id) {
        pacientService.deletePatient(id);
        return "redirect:/admin/pacienti";
    }
    
    @GetMapping("/pacienti/add")
    public String addPatientForm(Model model) {
        model.addAttribute("patient", new Pacient());
        return "admin/add_patient";
    }
    
    @PostMapping("/pacienti/add")
    public String addPatient(Pacient patient) {
        pacientService.createPatient(patient);
        return "redirect:/admin/pacienti";
    }
    
    // Medication Management
    @GetMapping("/medicamente")
    public String listMedications(Model model) {
        model.addAttribute("medicamente", medicamentService.getAllMedications());
        return "admin/medications";
    }
    
    @GetMapping("/medicamente/edit/{id}")
    public String editMedicationForm(@PathVariable Long id, Model model) {
        Medicament medicament = medicamentService.getMedicationById(id);
        model.addAttribute("medicament", medicament);
        return "admin/edit_medication";
    }
    
    @PostMapping("/medicamente/edit/{id}")
    public String updateMedication(@PathVariable Long id, Medicament medicament) {
        medicamentService.updateMedication(id, medicament);
        return "redirect:/admin/medicamente";
    }
    
    @GetMapping("/medicamente/delete/{id}")
    public String deleteMedication(@PathVariable Long id) {
        medicamentService.deleteMedication(id);
        return "redirect:/admin/medicamente";
    }
    
    @GetMapping("/medicamente/add")
    public String addMedicationForm(Model model) {
        model.addAttribute("medicament", new Medicament());
        return "admin/add_medication";
    }
    
    @PostMapping("/medicamente/add")
    public String addMedication(Medicament medicament) {
        medicamentService.createMedication(medicament);
        return "redirect:/admin/medicamente";
    }
}

