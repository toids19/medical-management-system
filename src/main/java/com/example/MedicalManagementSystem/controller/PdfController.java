package com.example.MedicalManagementSystem.controller;

import com.example.MedicalManagementSystem.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayInputStream;

@Controller
@RequestMapping("/pdf")
public class PdfController {

    private final ReportService reportService;

    @Autowired
    public PdfController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/patients")
    public ResponseEntity<InputStreamResource> generatePatientsPdf() {
        try {
            ByteArrayInputStream pdfStream = reportService.generatePatientsReport();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=raport-pacienti.pdf");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(pdfStream));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
}

