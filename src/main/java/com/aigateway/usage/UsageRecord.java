package com.aigateway.usage;

import com.aigateway.apikey.ApiKey;
import com.aigateway.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usage_records")
@EntityListeners(AuditingEntityListener.class)
public class UsageRecord {

    public enum Status { SUCCESS, ERROR, RATE_LIMITED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_key_id")
    private ApiKey apiKey;

    @Column(nullable = false)
    private String model;

    @Column(name = "prompt_tokens", nullable = false)
    @Builder.Default
    private int promptTokens = 0;

    @Column(name = "completion_tokens", nullable = false)
    @Builder.Default
    private int completionTokens = 0;

    @Column(name = "total_tokens", nullable = false)
    @Builder.Default
    private int totalTokens = 0;

    @Column(name = "latency_ms", nullable = false)
    @Builder.Default
    private int latencyMs = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.SUCCESS;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
