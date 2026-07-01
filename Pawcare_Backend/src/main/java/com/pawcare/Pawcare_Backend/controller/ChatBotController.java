package com.pawcare.Pawcare_Backend.controller;

import com.pawcare.Pawcare_Backend.dto.ChatRequest;
import com.pawcare.Pawcare_Backend.dto.ChatResponse;
import com.pawcare.Pawcare_Backend.service.ChatBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/chatbot")
public class ChatBotController {

    @Autowired
    private ChatBotService chatbotService;

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        ChatResponse response = chatbotService.getChatResponse(request.getMessage());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/help")
    public ResponseEntity<String> getHelp() {
        return ResponseEntity.ok(
                "I can help with:\n" +
                        "- Emergency first aid\n" +
                        "- Animal care tips\n" +
                        "- Poisoning information\n" +
                        "- Lost/found animals\n" +
                        "- Adoption guidance\n" +
                        "- Vaccination schedules\n" +
                        "Just ask me anything about animals! 🐾"
        );
    }
}