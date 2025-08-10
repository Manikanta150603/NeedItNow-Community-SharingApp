package com.needitnow.repositories;

import com.needitnow.entity.NeighborMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NeighborMessageRepository extends JpaRepository<NeighborMessage, Long> {
    List<NeighborMessage> findByRequestId(Long requestId);
    List<NeighborMessage> findByRequestIdOrderBySentAtAsc(Long requestId);
    List<NeighborMessage> findBySenderId(Long senderId);
    List<NeighborMessage> findByReceiverId(Long receiverId);
    List<NeighborMessage> findByReceiverIdAndIsReadFalse(Long receiverId);
    
    // Find messages exchanged between two users for a specific request
    List<NeighborMessage> findByRequestIdAndSenderIdAndReceiverIdOrderBySentAtAsc(Long requestId, Long senderId, Long receiverId);
    
    // Find all messages related to a request where user is either sender or receiver
    @Query("SELECT m FROM NeighborMessage m WHERE m.request.id = :requestId AND (m.sender.id = :userId OR m.receiver.id = :userId) ORDER BY m.sentAt ASC")
    List<NeighborMessage> findUserMessagesForRequest(@Param("requestId") Long requestId, @Param("userId") Long userId);
    
    // Find all messages for a specific request sent by a specific user
    List<NeighborMessage> findByRequestIdAndSenderId(Long requestId, Long senderId);
}
