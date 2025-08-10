package com.needitnow.services;

import com.needitnow.entity.ChatMessage;
import com.needitnow.entity.Item;
import com.needitnow.repositories.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessage saveChatMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getChatMessages(Long itemId, Long user1Id, Long user2Id) {
        return chatMessageRepository.findChatHistory(itemId, user1Id, user2Id);
    }
    
    public List<Item> getItemsWithChatMessages(Long userId) {
        return chatMessageRepository.findItemsWithChatMessages(userId);
    }
    
    public List<ChatMessage> getChatMessagesForItemOwner(Long itemId, Long ownerId) {
        return chatMessageRepository.findChatMessagesForItemOwner(itemId, ownerId);
    }
    
    public List<ChatMessage> getSentMessagesByUser(Long userId) {
        return chatMessageRepository.getSentMessagesByUser(userId);
    }
}