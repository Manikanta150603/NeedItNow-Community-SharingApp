package com.needitnow.services;

import com.needitnow.entity.Community;
import com.needitnow.repositories.CommunityRepository;
import com.needitnow.entity.CommunityJoinRequest;
import com.needitnow.repositories.CommunityJoinRequestRepository;
import com.needitnow.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommunityService {
    
    private final CommunityRepository communityRepository;
    private final CommunityJoinRequestRepository joinRequestRepository;

    @Autowired
    public CommunityService(CommunityRepository communityRepository, CommunityJoinRequestRepository joinRequestRepository) {
        this.communityRepository = communityRepository;
        this.joinRequestRepository = joinRequestRepository;
    }
    
    @Transactional
    public Community saveCommunity(Community community) {
        return communityRepository.save(community);
    }
    
    public List<Community> searchCommunities(String query) {
        return communityRepository.searchCommunities(query);
    }
    
    public Page<Community> searchCommunities(String query, Pageable pageable) {
        List<Community> results = searchCommunities(query);
        return new PageImpl<>(results, pageable, results.size());
    }
    
    public List<Community> findAll() {
        return communityRepository.findAll();
    }
    
    public List<Community> findNearbyCommunities(double latitude, double longitude, double radiusInKm) {
        return communityRepository.findNearbyCommunities(latitude, longitude, radiusInKm);
    }
    @Transactional(readOnly = true)
    public List<CommunityJoinRequest> getPendingRequestsForAdmin(Long adminId) {
        return joinRequestRepository.findPendingRequestsForAdmin(adminId);
    }

    @Transactional
    public String createJoinRequest(Long communityId, User user) {
        // Check if community exists
        Community community = communityRepository.findById(communityId)
            .orElseThrow(() -> new IllegalArgumentException("Community not found"));
            
        // Check if user is trying to join their own community
        if (community.getUser().getId().equals(user.getId())) {
            return "You cannot send a join request to your own community";
        }
        
        // Check if user already has a pending or approved request for this community
        boolean hasExistingRequest = joinRequestRepository.existsByCommunityIdAndUserIdAndStatusIn(
            communityId, 
            user.getId(), 
            List.of("PENDING", "APPROVED")
        );
        
        if (hasExistingRequest) {
            return "You already have a pending or approved request for this community";
        }
        
        // Create and save the join request
        CommunityJoinRequest joinRequest = new CommunityJoinRequest(
            community,
            user,
            "PENDING"
        );
        
        joinRequestRepository.save(joinRequest);
        return "Join request sent successfully";
    }

    public int getPendingRequestCount(Long adminId) {
        return joinRequestRepository.countPendingRequestsForAdmin(adminId);
    }

    public List<Community> getCommunitiesByUser(Long userId) {
        return communityRepository.findByUserId(userId);
    }
    
    @Transactional
    public String approveRequest(Long requestId, Long adminId) {
        CommunityJoinRequest request = joinRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (!request.getCommunity().getUser().getId().equals(adminId)) {
            throw new SecurityException("You don't have permission to approve this request");
        }
        
        request.setStatus("APPROVED");
        request.setStatusUpdatedAt(LocalDateTime.now()); // Add this line
        joinRequestRepository.save(request);
        return "Request approved successfully";
    }

    @Transactional
    public String rejectRequest(Long requestId, Long adminId) {
        CommunityJoinRequest request = joinRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (!request.getCommunity().getUser().getId().equals(adminId)) {
            throw new SecurityException("You don't have permission to reject this request");
        }
        
        request.setStatus("REJECTED");
        request.setStatusUpdatedAt(LocalDateTime.now()); // Add this line
        joinRequestRepository.save(request);
        return "Request rejected successfully";
    }
    
    
    
    @Transactional(readOnly = true)
    public List<Community> getCommunitiesWhereUserIsMember(Long userId) {
        // Get all communities where user is a member (approved) or owner
        List<Community> communities = communityRepository.findCommunitiesByMemberId(userId);
        
        // Get community IDs
        List<Long> communityIds = communities.stream()
            .map(Community::getId)
            .collect(Collectors.toList());
        
        // Get member counts for each community
        Map<Long, Long> memberCounts = joinRequestRepository.findMemberCountsForCommunities(communityIds)
            .stream()
            .collect(Collectors.toMap(
                arr -> (Long) arr[0],
                arr -> (Long) arr[1]
            ));
        
        // Set member counts on each community
        communities.forEach(community -> 
            community.setMemberCount(memberCounts.getOrDefault(community.getId(), 1L))
        );
        
        return communities;
    }
    
    @Transactional(readOnly = true)
    public java.util.Optional<Community> getCommunityById(Long communityId) {
        return communityRepository.findById(communityId);
    }
    
    @Transactional(readOnly = true)
    public boolean isUserMemberOfCommunity(Long userId, Long communityId) {
        // Check if user is the creator of the community
        if (communityRepository.existsByIdAndUserId(communityId, userId)) {
            return true;
        }
        
        // Check if user has an approved join request
        return joinRequestRepository.existsByUserIdAndCommunityIdAndStatus(userId, communityId, "APPROVED");
    }
    
    @Transactional(readOnly = true)
    public long getMemberCount(Long communityId) {
        // Count all approved members + 1 for the creator
        Long approvedMembers = joinRequestRepository.countByCommunityIdAndStatus(communityId, "APPROVED");
        return approvedMembers != null ? approvedMembers + 1 : 1;
    }
    
    @Transactional(readOnly = true)
    public List<CommunityJoinRequest> getPendingRequestsForUser(User user) {
        return joinRequestRepository.findByUserAndStatus(user, "PENDING");
    }
    
    @Transactional(readOnly = true)
    public int getUserPendingRequestCount(User user) {
        return joinRequestRepository.countByUserAndStatus(user, "PENDING");
    }
    
    @Transactional
    public String cancelRequest(Long requestId, Long userId) {
        CommunityJoinRequest request = joinRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (!request.getUser().getId().equals(userId)) {
            throw new SecurityException("You don't have permission to cancel this request");
        }
        
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Only pending requests can be canceled");
        }
        
        joinRequestRepository.delete(request);
        return "Request canceled successfully";
    }
    
}
