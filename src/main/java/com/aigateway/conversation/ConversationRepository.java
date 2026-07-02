package com.aigateway.conversation;

import com.aigateway.user.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Page<Conversation> findByUserOrderByUpdatedAtDesc(User user, Pageable pageable);

    Optional<Conversation> findByIdAndUser(Long id, User user);
}
