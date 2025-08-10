package com.needitnow.repositories;

import com.needitnow.entity.NeighborRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NeighborRequestRepository extends JpaRepository<NeighborRequest, Long> {
    List<NeighborRequest> findByCommunityId(Long communityId);
    List<NeighborRequest> findByRequesterId(Long requesterId);
    List<NeighborRequest> findByCommunityIdAndRequesterIdNot(Long communityId, Long userId);
    List<NeighborRequest> findByCommunityIdAndRequesterIdNotAndActiveTrue(Long communityId, Long userId);
    List<NeighborRequest> findByCommunityIdAndRequesterIdNotAndActiveTrueAndIsResolvedFalse(Long communityId, Long userId);
    List<NeighborRequest> findByCommunityIdAndRequesterId(Long communityId, Long userId);
    List<NeighborRequest> findByRequesterIdAndIsResolvedFalse(Long requesterId);
}
