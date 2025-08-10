package com.needitnow.repositories;

import com.needitnow.entity.GroupBuy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long> {
    List<GroupBuy> findByCommunityId(Long communityId);
    List<GroupBuy> findByCommunityIdAndActive(Long communityId, boolean active);
    List<GroupBuy> findByUserIdAndCommunityId(Long userId, Long communityId);
    List<GroupBuy> findByUserIdAndCommunityIdAndActive(Long userId, Long communityId, boolean active);
    List<GroupBuy> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<GroupBuy> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}
