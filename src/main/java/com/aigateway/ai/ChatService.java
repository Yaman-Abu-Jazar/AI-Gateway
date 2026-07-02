package com.aigateway.ai;

import com.aigateway.ai.AiProvider.ChatCompletion;
import com.aigateway.ai.AiProvider.ChatMessage;
import com.aigateway.ai.AiProvider.ChatRequest;
import com.aigateway.ai.dto.ChatRequestDto;
import com.aigateway.ai.dto.ChatResponseDto;
import com.aigateway.apikey.ApiKey;
import com.aigateway.common.BadRequestException;
import com.aigateway.common.NotFoundException;
import com.aigateway.config.AppProperties;
import com.aigateway.conversation.Conversation;
import com.aigateway.conversation.ConversationRepository;
import com.aigateway.conversation.Message;
import com.aigateway.conversation.MessageRepository;
import com.aigateway.prompt.PromptTemplate;
import com.aigateway.prompt.PromptTemplateRepository;
import com.aigateway.ratelimit.RateLimitService;
import com.aigateway.security.ApiKeyAuthenticationFilter;
import com.aigateway.security.SecurityUtils;
import com.aigateway.usage.UsageRecord;
import com.aigateway.usage.UsageRepository;
import com.aigateway.user.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiProvider provider;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final PromptTemplateRepository templateRepository;
    private final UsageRepository usageRepository;
    private final RateLimitService rateLimitService;
    private final AppProperties props;

    @Transactional
    public ChatResponseDto chat(ChatRequestDto req, HttpServletRequest httpRequest) {
        User user = SecurityUtils.currentUser();
        rateLimitService.checkAndConsume(user);

        int maxTokens = req.maxTokens() != null
                ? req.maxTokens()
                : rateLimitService.limitsFor(user).getMaxTokensPerRequest();
        int planCap = rateLimitService.limitsFor(user).getMaxTokensPerRequest();
        if (maxTokens > planCap) {
            throw new BadRequestException("maxTokens " + maxTokens + " exceeds plan cap " + planCap);
        }

        Conversation conversation = loadOrCreateConversation(req, user);
        String systemPrompt = resolveSystemPrompt(req, user, conversation);

        List<ChatMessage> history = buildHistory(conversation, systemPrompt, req.message());
        Message userMessage = messageRepository.save(Message.builder()
                .conversation(conversation)
                .role(Message.MessageRole.USER)
                .content(req.message())
                .build());

        long started = System.currentTimeMillis();
        String model = req.model() != null ? req.model() : props.getAi().getOpenai().getDefaultModel();
        ChatCompletion result;
        try {
            result = provider.complete(new ChatRequest(model, history, maxTokens, req.temperature()));
        } catch (RuntimeException ex) {
            recordUsage(user, httpRequest, model, 0, 0, System.currentTimeMillis() - started,
                    UsageRecord.Status.ERROR);
            throw ex;
        }
        long latency = System.currentTimeMillis() - started;

        userMessage.setPromptTokens(result.promptTokens());
        messageRepository.save(userMessage);

        Message assistantMessage = messageRepository.save(Message.builder()
                .conversation(conversation)
                .role(Message.MessageRole.ASSISTANT)
                .content(result.content())
                .promptTokens(0)
                .completionTokens(result.completionTokens())
                .build());

        conversation.setModel(result.model());
        if (conversation.getTitle() == null || conversation.getTitle().isBlank()) {
            conversation.setTitle(deriveTitle(req.message()));
        }
        conversationRepository.save(conversation);

        recordUsage(user, httpRequest, result.model(), result.promptTokens(), result.completionTokens(),
                latency, UsageRecord.Status.SUCCESS);

        return new ChatResponseDto(
                conversation.getId(), assistantMessage.getId(), result.model(), result.content(),
                result.promptTokens(), result.completionTokens(), result.totalTokens(), latency);
    }

    private Conversation loadOrCreateConversation(ChatRequestDto req, User user) {
        if (req.conversationId() != null) {
            return conversationRepository.findByIdAndUser(req.conversationId(), user)
                    .orElseThrow(() -> new NotFoundException("Conversation not found"));
        }
        return conversationRepository.save(Conversation.builder()
                .user(user)
                .model(req.model())
                .build());
    }

    private String resolveSystemPrompt(ChatRequestDto req, User user, Conversation conversation) {
        if (req.promptTemplateId() != null) {
            PromptTemplate pt = templateRepository.findById(req.promptTemplateId())
                    .orElseThrow(() -> new NotFoundException("Prompt template not found"));
            boolean owned = pt.getUser().getId().equals(user.getId());
            if (!owned && !pt.isPublic()) {
                throw new NotFoundException("Prompt template not found");
            }
            if (conversation.getSystemPrompt() == null) {
                conversation.setSystemPrompt(pt.getContent());
            }
            return pt.getContent();
        }
        return conversation.getSystemPrompt();
    }

    private List<ChatMessage> buildHistory(Conversation conversation, String systemPrompt, String newUserMsg) {
        List<ChatMessage> out = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            out.add(new ChatMessage("system", systemPrompt));
        }
        for (Message m : messageRepository.findByConversationOrderByIdAsc(conversation)) {
            out.add(new ChatMessage(m.getRole().name().toLowerCase(), m.getContent()));
        }
        out.add(new ChatMessage("user", newUserMsg));
        return out;
    }

    private String deriveTitle(String msg) {
        String trimmed = msg.strip().replaceAll("\\s+", " ");
        return trimmed.length() <= 60 ? trimmed : trimmed.substring(0, 57) + "...";
    }

    private void recordUsage(User user, HttpServletRequest req, String model,
                             int prompt, int completion, long latencyMs, UsageRecord.Status status) {
        ApiKey key = ApiKeyAuthenticationFilter.currentApiKey(req);
        usageRepository.save(UsageRecord.builder()
                .user(user)
                .apiKey(key)
                .model(model)
                .promptTokens(prompt)
                .completionTokens(completion)
                .totalTokens(prompt + completion)
                .latencyMs((int) Math.min(Integer.MAX_VALUE, latencyMs))
                .status(status)
                .build());
    }
}
