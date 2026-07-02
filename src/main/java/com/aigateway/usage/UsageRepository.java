package com.aigateway.usage;

import com.aigateway.user.User;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsageRepository extends JpaRepository<UsageRecord, Long> {

    @Query("""
            SELECT COALESCE(SUM(u.totalTokens), 0)
            FROM UsageRecord u
            WHERE u.user = :user AND u.createdAt >= :since
            """)
    long sumTokensSince(@Param("user") User user, @Param("since") Instant since);

    @Query("""
            SELECT COUNT(u)
            FROM UsageRecord u
            WHERE u.user = :user AND u.createdAt >= :since
            """)
    long countRequestsSince(@Param("user") User user, @Param("since") Instant since);
}
