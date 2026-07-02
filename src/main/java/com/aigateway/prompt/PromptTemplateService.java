package com.aigateway.prompt;

import com.aigateway.common.NotFoundException;
import com.aigateway.prompt.dto.PromptTemplateRequest;
import com.aigateway.prompt.dto.PromptTemplateResponse;
import com.aigateway.security.SecurityUtils;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromptTemplateService {

    private final PromptTemplateRepository repository;

    @Transactional
    public PromptTemplateResponse create(PromptTemplateRequest req) {
        PromptTemplate saved = repository.save(PromptTemplate.builder()
                .user(SecurityUtils.currentUser())
                .name(req.name())
                .description(req.description())
                .content(req.content())
                .isPublic(req.isPublic())
                .build());
        return toDto(saved);
    }

    @Transactional
    public PromptTemplateResponse update(Long id, PromptTemplateRequest req) {
        PromptTemplate pt = repository.findByIdAndUser(id, SecurityUtils.currentUser())
                .orElseThrow(() -> new NotFoundException("Template not found"));
        pt.setName(req.name());
        pt.setDescription(req.description());
        pt.setContent(req.content());
        pt.setPublic(req.isPublic());
        return toDto(repository.save(pt));
    }

    @Transactional
    public void delete(Long id) {
        PromptTemplate pt = repository.findByIdAndUser(id, SecurityUtils.currentUser())
                .orElseThrow(() -> new NotFoundException("Template not found"));
        repository.delete(pt);
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateResponse> listMine() {
        return repository.findByUserOrderByIdDesc(SecurityUtils.currentUser())
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateResponse> listAvailable() {
        // Own + public, de-duped by id
        Stream<PromptTemplate> mine = repository.findByUserOrderByIdDesc(SecurityUtils.currentUser()).stream();
        Stream<PromptTemplate> pub = repository.findByIsPublicTrueOrderByIdDesc().stream();
        return Stream.concat(mine, pub)
                .collect(java.util.stream.Collectors.toMap(PromptTemplate::getId, t -> t, (a, b) -> a))
                .values().stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .map(this::toDto)
                .toList();
    }

    private PromptTemplateResponse toDto(PromptTemplate p) {
        return new PromptTemplateResponse(p.getId(), p.getName(), p.getDescription(),
                p.getContent(), p.isPublic(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
