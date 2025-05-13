package com.example.MedicalManagementSystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ChatGptService {

    private static final Logger logger = LoggerFactory.getLogger(ChatGptService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openai.api.key:demo-key}")
    private String apiKey;

    private final String apiUrl = "https://api.openai.com/v1/chat/completions";

    public String generateResponse(String prompt) {
        logger.info("Generating AI response for prompt: {}", prompt);

        // If we're using the demo key, return a mock response
        if ("demo-key".equals(apiKey)) {
            logger.info("Using demo key, returning mock response");
            return generateMockResponse(prompt);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();

            rootNode.put("model", "gpt-3.5-turbo");

            ArrayNode messagesNode = mapper.createArrayNode();

            // System message to instruct the AI to respond in Romanian
            ObjectNode systemMessage = mapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Ești un asistent medical în limba română. Oferă informații medicale precise și utile. Răspunde întotdeauna în limba română, chiar dacă utilizatorul folosește o altă limbă.");
            messagesNode.add(systemMessage);

            // User message
            ObjectNode userMessage = mapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messagesNode.add(userMessage);

            rootNode.set("messages", messagesNode);
            rootNode.put("temperature", 0.7);

            HttpEntity<String> request = new HttpEntity<>(rootNode.toString(), headers);

            logger.info("Sending request to OpenAI API");
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            logger.info("Received response from OpenAI API: {}", response.getStatusCode());

            JsonNode responseBody = mapper.readTree(response.getBody());
            String generatedText = responseBody
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            logger.info("Successfully generated AI response");
            return generatedText;

        } catch (Exception e) {
            logger.error("Error generating AI response", e);
            return "Ne pare rău, nu am putut genera un răspuns. Vă rugăm să încercați din nou mai târziu. Eroare: " + e.getMessage();
        }
    }

    private String generateMockResponse(String prompt) {
        // Simple mock responses for demo purposes
        prompt = prompt.toLowerCase();

        if (prompt.contains("salut") || prompt.contains("buna") || prompt.contains("hello")) {
            return "Bună ziua! Sunt asistentul medical virtual. Cu ce vă pot ajuta astăzi?";
        } else if (prompt.contains("durere") || prompt.contains("doare")) {
            return "Durerea poate avea multe cauze. Vă recomand să consultați un medic pentru un diagnostic corect. Între timp, puteți încerca să vă odihniți și să luați un analgezic dacă este necesar.";
        } else if (prompt.contains("medicament") || prompt.contains("tratament")) {
            return "Nu pot recomanda medicamente specifice fără prescripție medicală. Vă rog să consultați medicul dumneavoastră pentru un tratament adecvat.";
        } else if (prompt.contains("covid") || prompt.contains("virus")) {
            return "Simptomele COVID-19 includ febră, tuse, oboseală și pierderea gustului sau mirosului. Dacă suspectați că aveți COVID-19, izolați-vă și contactați medicul pentru testare.";
        } else {
            return "Înțeleg întrebarea dumneavoastră despre '" + prompt + "'. Pentru informații medicale precise, vă recomand să consultați un medic specialist. Pot oferi doar informații generale.";
        }
    }
}
