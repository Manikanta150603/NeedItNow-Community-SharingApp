package com.needitnow.repositories;

import com.needitnow.entity.Item;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByCommunityId(Long communityId);
    List<Item> findByUserIdAndRequesterIsNotNull(Long userId);
    
    // Find items shared by a specific user in a community
    List<Item> findByUserIdAndCommunityId(Long userId, Long communityId);
    
    // Find items requested by a specific user in a community
    List<Item> findByRequesterIdAndCommunityId(Long userId, Long communityId);
}