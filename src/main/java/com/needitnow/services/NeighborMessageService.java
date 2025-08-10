package com.needitnow.services;

import com.needitnow.entity.NeighborMessage;
import com.needitnow.repositories.NeighborMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NeighborMessageService {
    private final NeighborMessageRepository neighborMessageRepository;

    @Autowired
    public NeighborMessageService(NeighborMessageRepository neighborMessageRepository) {
        this.neighborMessageRepository = neighborMessageRepository;
    }

    public NeighborMessage saveMessage(NeighborMessage message) {
        return neighborMessageRepository.save(message);
    }
    
    public List<NeighborMessage> getMessagesByRequest(Long requestId) {
        return neighborMessageRepository.findByRequestIdOrderBySentAtAsc(requestId);
    }
    
    public List<NeighborMessage> getConversation(Long requestId, Long userId1, Long userId2) {
        return neighborMessageRepository.findByRequestIdAndSenderIdAndReceiverIdOrderBySentAtAsc(requestId, userId1, userId2);
    }
    
    public List<NeighborMessage> getUserMessagesForRequest(Long requestId, Long userId) {
        return neighborMessageRepository.findUserMessagesForRequest(requestId, userId);
    }
    
    public List<NeighborMessage> getUnreadMessagesByUser(Long userId) {
        return neighborMessageRepository.findByReceiverIdAndIsReadFalse(userId);
    }
    
    /**
     * Checks if a user has responded to a specific request
     * @param requestId The ID of the request
     * @param userId The ID of the user to check
     * @return true if the user has sent any messages for this request, false otherwise
     */
    public boolean hasRespondedToRequest(Long requestId, Long userId) {
        List<NeighborMessage> userResponses = neighborMessageRepository.findByRequestIdAndSenderId(requestId, userId);
        return userResponses != null && !userResponses.isEmpty();
    }
    
    public void markMessagesAsRead(Long requestId, Long receiverId) {
        List<NeighborMessage> messages = neighborMessageRepository.findByRequestId(requestId).stream()
                .filter(msg -> msg.getReceiver().getId().equals(receiverId) && !msg.isRead())
                .collect(Collectors.toList());
        
        messages.forEach(msg -> {
            msg.setRead(true);
            neighborMessageRepository.save(msg);
        });
    }
    
    public int getUnreadMessageCount(Long userId) {
        return neighborMessageRepository.findByReceiverIdAndIsReadFalse(userId).size();
    }
}
