package com.needitnow.repositories;

import com.needitnow.entity.ChatMessage;
import com.needitnow.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.item.id = :itemId " +
           "AND ((cm.sender.id = :user1Id AND cm.receiver.id = :user2Id) " +
           "OR (cm.sender.id = :user2Id AND cm.receiver.id = :user1Id)) " +
           "ORDER BY cm.timestamp ASC")
    List<ChatMessage> findChatHistory(@Param("itemId") Long itemId,
                                      @Param("user1Id") Long user1Id,
                                      @Param("user2Id") Long user2Id);

    @Query("SELECT DISTINCT cm.item FROM ChatMessage cm WHERE cm.item.user.id = :userId")
    List<Item> findItemsWithChatMessages(@Param("userId") Long userId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.item.id = :itemId AND cm.item.user.id = :ownerId ORDER BY cm.timestamp DESC")
    List<ChatMessage> findChatMessagesForItemOwner(@Param("itemId") Long itemId, @Param("ownerId") Long ownerId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.sender.id = :userId ORDER BY cm.timestamp DESC")
    List<ChatMessage> getSentMessagesByUser(@Param("userId") Long userId);
}