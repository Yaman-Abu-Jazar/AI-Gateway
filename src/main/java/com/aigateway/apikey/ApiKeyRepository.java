package com.aigateway.apikey;

import com.aigateway.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    List<ApiKey> findByUserOrderByIdDesc(User user);

    @Query("SELECT k FROM ApiKey k JOIN FETCH k.user WHERE k.keyHash = :hash")
    Optional<ApiKey> findByKeyHashWithUser(@Param("hash") String hash);
}
