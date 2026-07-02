package com.aigateway.apikey;

import com.aigateway.apikey.dto.ApiKeyResponse;
import com.aigateway.apikey.dto.CreateApiKeyRequest;
import com.aigateway.apikey.dto.NewApiKeyResponse;
import com.aigateway.common.NotFoundException;
import com.aigateway.security.ApiKeyHasher;
import com.aigateway.security.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository repository;
    private final ApiKeyHasher hasher;

    @Transactional
    public NewApiKeyResponse create(CreateApiKeyRequest req) {
        String plain = hasher.generatePlainKey();
        ApiKey saved = repository.save(ApiKey.builder()
                .user(SecurityUtils.currentUser())
                .name(req.name())
                .keyPrefix(hasher.prefix(plain))
                .keyHash(hasher.hash(plain))
                .build());
        return new NewApiKeyResponse(toDto(saved), plain);
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> list() {
        return repository.findByUserOrderByIdDesc(SecurityUtils.currentUser())
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public void revoke(Long id) {
        ApiKey key = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("API key not found"));
        if (!key.getUser().getId().equals(SecurityUtils.currentUser().getId())) {
            throw new NotFoundException("API key not found");
        }
        key.setRevoked(true);
        repository.save(key);
    }

    private ApiKeyResponse toDto(ApiKey k) {
        return new ApiKeyResponse(k.getId(), k.getName(), k.getKeyPrefix(),
                k.isRevoked(), k.getLastUsedAt(), k.getCreatedAt());
    }
}
