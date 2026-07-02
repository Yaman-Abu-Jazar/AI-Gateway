package com.aigateway.ai;

import com.aigateway.ai.dto.ChatRequestDto;
import com.aigateway.ai.dto.ChatResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat", description = "Send a message to the AI. Rate-limited per plan and usage-tracked.")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "Send a chat message and receive a completion")
    @PostMapping
    public ChatResponseDto chat(@Valid @RequestBody ChatRequestDto req, HttpServletRequest httpRequest) {
        return chatService.chat(req, httpRequest);
    }
}
