package com.example.MedicalManagementSystem.controller;

import com.example.MedicalManagementSystem.model.Pacient;
import com.example.MedicalManagementSystem.service.OcrService;
import com.example.MedicalManagementSystem.service.PacientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user/ocr")
@PreAuthorize("hasRole('USER')")
public class OcrController {

    @Autowired
    private OcrService ocrService;

    @Autowired
    private PacientService pacientService;

    @GetMapping
    public String showOcrPage() {
        return "user/ocr-page";
    }

    @PostMapping("/extract")
    @ResponseBody
    public Map<String, Object> extractText(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            String extractedText = ocrService.extractTextFromDocument(file);
            Map<String, String> patientInfo = ocrService.extractPatientInfo(extractedText);

            response.put("success", true);
            response.put("extractedText", extractedText);
            response.put("patientInfo", patientInfo);

            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Eroare la procesarea documentului: " + e.getMessage());
            e.printStackTrace();
            return response;
        }
    }

    @PostMapping("/create-patient")
    public String createPatientFromDocument(@RequestParam("file") MultipartFile file,
                                            RedirectAttributes redirectAttributes) {
        try {
            String extractedText = ocrService.extractTextFromDocument(file);
            Map<String, String> patientInfo = ocrService.extractPatientInfo(extractedText);

            Pacient patient = new Pacient();

            // Split name into first and last name if available
            if (patientInfo.containsKey("name")) {
                String[] nameParts = patientInfo.get("name").split("\\s+", 2);
                if (nameParts.length > 1) {
                    patient.setNume(nameParts[0]);
                    patient.setPrenume(nameParts[1]);
                } else {
                    patient.setNume(nameParts[0]);
                    patient.setPrenume("");
                }
            }

            patient.setCnp(patientInfo.getOrDefault("cnp", ""));
            patient.setObservatii("Extras din document OCR: " +
                    (extractedText.length() > 200 ? extractedText.substring(0, 200) + "..." : extractedText));

            pacientService.createPatient(patient);

            redirectAttributes.addFlashAttribute("success", "Pacient creat cu succes din documentul procesat.");
            return "redirect:/user/pacienti";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la procesarea documentului: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/user/ocr";
        }
    }

    @PostMapping("/save-patient")
    public String savePatient(@ModelAttribute Pacient patient, RedirectAttributes redirectAttributes) {
        try {
            pacientService.createPatient(patient);
            redirectAttributes.addFlashAttribute("success", "Pacient salvat cu succes.");
            return "redirect:/user/pacienti";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la salvarea pacientului: " + e.getMessage());
            return "redirect:/user/ocr";
        }
    }

    @GetMapping("/download-text")
    public ResponseEntity<ByteArrayResource> downloadText(@RequestParam String text) {
        return ocrService.getTextAsResource(text);
    }
}
