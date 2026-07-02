package com.aigateway.conversation;

import com.aigateway.common.NotFoundException;
import com.aigateway.conversation.dto.ConversationDetail;
import com.aigateway.conversation.dto.ConversationSummary;
import com.aigateway.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Conversations", description = "Browse and delete stored conversations")
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Operation(summary = "List the current user's conversations (paged, newest first)")
    @GetMapping
    public Page<ConversationSummary> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return conversationRepository
                .findByUserOrderByUpdatedAtDesc(SecurityUtils.currentUser(), pageable)
                .map(c -> new ConversationSummary(c.getId(), c.getTitle(), c.getModel(),
                        c.getCreatedAt(), c.getUpdatedAt()));
    }

    @Operation(summary = "Get a single conversation, including all its messages")
    @GetMapping("/{id}")
    public ConversationDetail get(@PathVariable Long id) {
        Conversation c = conversationRepository.findByIdAndUser(id, SecurityUtils.currentUser())
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        var messages = messageRepository.findByConversationOrderByIdAsc(c).stream()
                .map(m -> new ConversationDetail.MessageView(
                        m.getId(), m.getRole().name(), m.getContent(),
                        m.getPromptTokens(), m.getCompletionTokens(), m.getCreatedAt()))
                .toList();
        return new ConversationDetail(c.getId(), c.getTitle(), c.getModel(),
                c.getSystemPrompt(), c.getCreatedAt(), c.getUpdatedAt(), messages);
    }

    @Operation(summary = "Delete a conversation and all its messages")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Conversation c = conversationRepository.findByIdAndUser(id, SecurityUtils.currentUser())
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        conversationRepository.delete(c);
        return ResponseEntity.noContent().build();
    }
}
