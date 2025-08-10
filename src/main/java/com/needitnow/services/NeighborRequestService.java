package com.needitnow.services;

import com.needitnow.entity.NeighborRequest;
import com.needitnow.repositories.NeighborRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NeighborRequestService {
    private final NeighborRequestRepository neighborRequestRepository;

    @Autowired
    public NeighborRequestService(NeighborRequestRepository neighborRequestRepository) {
        this.neighborRequestRepository = neighborRequestRepository;
    }

    public NeighborRequest saveRequest(NeighborRequest request) {
        return neighborRequestRepository.save(request);
    }
    
    public List<NeighborRequest> getRequestsByCommunity(Long communityId) {
        return neighborRequestRepository.findByCommunityId(communityId);
    }
    
    public List<NeighborRequest> getRequestsByUser(Long userId) {
        return neighborRequestRepository.findByRequesterId(userId);
    }
    
    public List<NeighborRequest> getActiveRequestsByUser(Long userId) {
        return neighborRequestRepository.findByRequesterIdAndIsResolvedFalse(userId);
    }
    
    public List<NeighborRequest> getRequestsByUserInCommunity(Long userId, Long communityId) {
        return neighborRequestRepository.findByCommunityIdAndRequesterId(communityId, userId);
    }
    
    public List<NeighborRequest> getOtherRequestsInCommunity(Long communityId, Long userId) {
        // Only return active requests that aren't from the current user
        return neighborRequestRepository.findByCommunityIdAndRequesterIdNotAndActiveTrueAndIsResolvedFalse(communityId, userId);
    }
    
    public Optional<NeighborRequest> getRequestById(Long requestId) {
        return neighborRequestRepository.findById(requestId);
    }
    
    public void markRequestAsResolved(Long requestId) {
        neighborRequestRepository.findById(requestId).ifPresent(request -> {
            request.setResolved(true);
            request.setResolvedAt(LocalDateTime.now());
            neighborRequestRepository.save(request);
        });
    }
    
    public void deleteRequest(Long requestId) {
        neighborRequestRepository.findById(requestId).ifPresent(request -> {
            // Soft delete by marking as inactive instead of physically deleting
            request.setActive(false);
            neighborRequestRepository.save(request);
        });
    }
}
