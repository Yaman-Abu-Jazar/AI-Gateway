package com.aigateway.prompt;

import com.aigateway.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    List<PromptTemplate> findByUserOrderByIdDesc(User user);

    List<PromptTemplate> findByIsPublicTrueOrderByIdDesc();

    Optional<PromptTemplate> findByIdAndUser(Long id, User user);
}
