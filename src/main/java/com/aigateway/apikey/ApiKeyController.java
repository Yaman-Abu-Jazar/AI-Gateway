package com.aigateway.apikey;

import com.aigateway.apikey.dto.ApiKeyResponse;
import com.aigateway.apikey.dto.CreateApiKeyRequest;
import com.aigateway.apikey.dto.NewApiKeyResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "API Keys", description = "Manage machine-to-machine API keys")
@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService service;

    @Operation(summary = "Create a new API key. The plaintext value is only returned once.")
    @PostMapping
    public ResponseEntity<NewApiKeyResponse> create(@Valid @RequestBody CreateApiKeyRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @Operation(summary = "List the current user's API keys (metadata only)")
    @GetMapping
    public List<ApiKeyResponse> list() {
        return service.list();
    }

    @Operation(summary = "Revoke an API key")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable Long id) {
        service.revoke(id);
        return ResponseEntity.noContent().build();
    }
}
