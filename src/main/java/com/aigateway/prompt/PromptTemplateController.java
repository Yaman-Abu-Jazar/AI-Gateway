package com.aigateway.prompt;

import com.aigateway.prompt.dto.PromptTemplateRequest;
import com.aigateway.prompt.dto.PromptTemplateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Prompt Templates", description = "Save and reuse prompt templates")
@RestController
@RequestMapping("/api/v1/prompt-templates")
@RequiredArgsConstructor
public class PromptTemplateController {

    private final PromptTemplateService service;

    @Operation(summary = "List templates (own by default, or set includePublic=true to include public ones)")
    @GetMapping
    public List<PromptTemplateResponse> list(@RequestParam(defaultValue = "false") boolean includePublic) {
        return includePublic ? service.listAvailable() : service.listMine();
    }

    @Operation(summary = "Create a new prompt template")
    @PostMapping
    public ResponseEntity<PromptTemplateResponse> create(@Valid @RequestBody PromptTemplateRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @Operation(summary = "Update an existing template")
    @PutMapping("/{id}")
    public PromptTemplateResponse update(@PathVariable Long id, @Valid @RequestBody PromptTemplateRequest req) {
        return service.update(id, req);
    }

    @Operation(summary = "Delete a template")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
