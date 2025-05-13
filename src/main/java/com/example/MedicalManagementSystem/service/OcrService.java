package com.example.MedicalManagementSystem.service;

import com.example.MedicalManagementSystem.model.Pacient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

@Service
public class OcrService {

    @Autowired
    private PacientService pacientService;

    private final Path uploadDir = Paths.get(System.getProperty("java.io.tmpdir"), "ocr-uploads");

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(uploadDir);
    }

    public String extractTextFromDocument(MultipartFile file) throws IOException {
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        String raw;
        if (filename.toLowerCase().endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(file.getInputStream())) {
                raw = new PDFTextStripper().getText(doc).trim();
                if (!raw.isEmpty()) {
                    return cleanUpText(raw);
                }
            }
            raw = ocrPdfWithCli(file);
        } else {
            raw = ocrImageWithCli(file);
        }
        return cleanUpText(raw);
    }

    private String cleanUpText(String text) {
        // Remove Tesseract’s resolution estimate line
        return text.replaceAll("^Estimating resolution as \\d+\\s*\\r?\\n", "");
    }

    private String ocrPdfWithCli(MultipartFile pdfFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (PDDocument doc = PDDocument.load(pdfFile.getInputStream())) {
            PDFRenderer renderer = new PDFRenderer(doc);
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                BufferedImage page = renderer.renderImageWithDPI(i, 300);
                Path tmpPng = uploadDir.resolve(UUID.randomUUID() + ".png");
                ImageIO.write(page, "PNG", tmpPng.toFile());

                sb.append(runTesseractCli(tmpPng)).append("\n\n");
                Files.delete(tmpPng);
            }
        }
        return sb.toString().trim();
    }

    private String ocrImageWithCli(MultipartFile file) throws IOException {
        String ext = getFileExtension(file.getOriginalFilename());
        Path tmp = uploadDir.resolve(UUID.randomUUID() + ext);
        file.transferTo(tmp.toFile());

        String result = runTesseractCli(tmp);
        Files.delete(tmp);
        return result.trim();
    }

    private String runTesseractCli(Path imagePath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "tesseract",
                imagePath.toAbsolutePath().toString(),
                "stdout",
                "-l", "ron"
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }
            if (p.waitFor() != 0) {
                throw new IOException("Tesseract CLI exited with code " + p.exitValue());
            }
            return out.toString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for tesseract", e);
        }
    }

    public Map<String, String> extractPatientInfo(String text) {
        Map<String, String> info = new HashMap<>();

        // Extract name
        Matcher mName = Pattern.compile("va informam ca\\s+([A-Z]+\\s+[A-Z]+)", Pattern.CASE_INSENSITIVE)
                .matcher(text);
        if (mName.find()) {
            info.put("name", mName.group(1).trim());
        }

        // Extract CNP
        Matcher mCnp = Pattern.compile("asigurare\\s+(\\d{13})", Pattern.CASE_INSENSITIVE)
                .matcher(text);
        if (mCnp.find()) {
            info.put("cnp", mCnp.group(1));
        }

        // Extract diagnosis by header
        Matcher mDiag = Pattern.compile(
                "(?i)Diagnosticul si codul de diagnostic:.*?\\n\\s*(\\d+)\\s+([^\\n]+)"
        ).matcher(text);
        if (mDiag.find()) {
            info.put("diagnosis", mDiag.group(1) + " " + mDiag.group(2).trim());
        }

        return info;
    }

    public Pacient createPatientFromOcr(MultipartFile file) throws IOException {
        String extracted = extractTextFromDocument(file);
        Map<String, String> info = extractPatientInfo(extracted);

        Pacient p = new Pacient();
        if (info.containsKey("name")) {
            String[] parts = info.get("name").split("\\s+", 2);
            p.setNume(parts[0]);
            p.setPrenume(parts.length > 1 ? parts[1] : "");
        } else {
            p.setNume("Unknown");
            p.setPrenume(UUID.randomUUID().toString().substring(0, 8));
        }
        p.setCnp(info.getOrDefault("cnp", ""));
        String obs = extracted.length() > 200
                ? extracted.substring(0, 200) + "..."
                : extracted;
        p.setObservatii("Extras din document OCR: " + obs);

        return pacientService.createPatient(p);
    }

    public ResponseEntity<ByteArrayResource> getTextAsResource(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ByteArrayResource res = new ByteArrayResource(bytes);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "text_extras.txt");
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(bytes.length)
                .body(res);
    }

    private String getFileExtension(String filename) {
        if (filename == null) return ".tmp";
        int dot = filename.lastIndexOf('.');
        return (dot < 0) ? ".tmp" : filename.substring(dot);
    }
}
