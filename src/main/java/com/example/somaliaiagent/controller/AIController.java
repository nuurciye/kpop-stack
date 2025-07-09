package com.example.somaliaiagent.controller;

import com.example.somaliaiagent.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// DTO for AI message request
class AiMessageRequest {
    private String message;
    private String language; // "en", "so"
    // Add other relevant context if needed, e.g., userId, sessionId

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}


@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    @Autowired
    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    // POST /api/ai/message: Process AI messages (generic endpoint if needed outside Twilio)
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> processAiMessage(@RequestBody AiMessageRequest request) {
        if (request.getMessage() == null || request.getMessage().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message content cannot be empty."));
        }
        String lang = (request.getLanguage() != null && !request.getLanguage().isEmpty()) ? request.getLanguage() : "en";

        // This is a generic processing, might need more context for specific tasks
        // For example, intent classification could happen here or in AIService
        String intent = aiService.classifyIntent(request.getMessage(), lang);
        String responseText = aiService.processUserMessage(request.getMessage(), lang);

        Map<String, Object> response = Map.of(
                "originalMessage", request.getMessage(),
                "language", lang,
                "detectedIntent", intent,
                "responseText", responseText
        );
        return ResponseEntity.ok(response);
    }

    // Example endpoint for specific AI task like information extraction
    // POST /api/ai/extract
    // @PostMapping("/extract")
    // public ResponseEntity<Map<String, Object>> extractInformation(@RequestBody AiExtractRequest extractRequest) {
    //     Map<String, Object> extractedData = aiService.extractInformation(
    //         extractRequest.getMessage(),
    //         extractRequest.getTaskDescription(),
    //         extractRequest.getLanguage()
    //     );
    //     return ResponseEntity.ok(extractedData);
    // }
}
