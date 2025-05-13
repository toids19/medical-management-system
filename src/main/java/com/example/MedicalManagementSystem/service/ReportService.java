package com.example.MedicalManagementSystem.service;

import com.example.MedicalManagementSystem.model.Pacient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final PacientService pacientService;
    private final PdfGeneratorService pdfGeneratorService;

    @Autowired
    public ReportService(PacientService pacientService, PdfGeneratorService pdfGeneratorService) {
        this.pacientService = pacientService;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    public ByteArrayInputStream generatePatientsReport() {
        List<Pacient> patients = pacientService.getAllPatients();

        Map<String, Object> data = new HashMap<>();
        data.put("patients", patients);
        data.put("generatedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("totalPatients", patients.size());

        return pdfGeneratorService.generatePdf("pdf/patients-pdf", data);
    }

    // You can add more report generation methods here for different types of reports
}

