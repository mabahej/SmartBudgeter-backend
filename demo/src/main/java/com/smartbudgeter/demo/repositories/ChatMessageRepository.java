package com.smartbudgeter.demo.repositories;

import com.smartbudgeter.demo.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop2BySenderOrderByIdDesc(String sender);
}
