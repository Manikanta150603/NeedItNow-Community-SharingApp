package com.needitnow.services;

import com.needitnow.entity.GroupBuy;

import com.needitnow.repositories.GroupBuyMemberRepository;
import com.needitnow.repositories.GroupBuyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class GroupBuyService {

    @Autowired
    private GroupBuyRepository groupBuyRepository;
    
    @Autowired
    private GroupBuyMemberRepository groupBuyMemberRepository;

    public GroupBuy saveGroupBuy(GroupBuy groupBuy) {
        return groupBuyRepository.save(groupBuy);
    }
    
    public GroupBuy save(GroupBuy groupBuy) {
        return saveGroupBuy(groupBuy);
    }

    public Optional<GroupBuy> findById(Long id) {
        return groupBuyRepository.findById(id);
    }

    public List<GroupBuy> findAllByCommunityId(Long communityId) {
        return groupBuyRepository.findByCommunityId(communityId);
    }

    public List<GroupBuy> findActiveByCommunityId(Long communityId) {
        return groupBuyRepository.findByCommunityIdAndActive(communityId, true);
    }

    public List<GroupBuy> findByUserIdAndCommunityId(Long userId, Long communityId) {
        return groupBuyRepository.findByUserIdAndCommunityId(userId, communityId);
    }

    public List<GroupBuy> findActiveByUserIdAndCommunityId(Long userId, Long communityId) {
        return groupBuyRepository.findByUserIdAndCommunityIdAndActive(userId, communityId, true);
    }

    public void deleteGroupBuy(Long id) {
        groupBuyRepository.deleteById(id);
    }
    
    public List<GroupBuy> findOrganizedByUser(Long userId) {
        return groupBuyRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<GroupBuy> findJoinedByUser(Long userId) {
        return groupBuyMemberRepository.findGroupBuysByUserIdAndIsCreatorFalse(userId);
    }
    
    public void lockGroupBuy(GroupBuy groupBuy) {
        groupBuy.setLocked(true);
        groupBuy.setStatus("LOCKED");
        groupBuyRepository.save(groupBuy);
    }
    
    public void completeGroupBuy(GroupBuy groupBuy) {
        groupBuy.setStatus("COMPLETED");
        groupBuyRepository.save(groupBuy);
    }
    
    public void cancelGroupBuy(GroupBuy groupBuy, String reason) {
        groupBuy.setActive(false);
        groupBuy.setStatus("CANCELLED");
        groupBuy.setCancellationReason(reason);
        groupBuyRepository.save(groupBuy);
    }
    
    public boolean isGroupBuyFull(GroupBuy groupBuy) {
        return groupBuy.getCurrentMemberCount() >= groupBuy.getMaxMembers();
    }
    
    public boolean isQuantityMaxReached(GroupBuy groupBuy) {
        return groupBuy.getCurrentQuantity() >= groupBuy.getMaxQuantity();
    }
    
    public String getGroupBuyUrgencyMessage(GroupBuy groupBuy) {
        if ("OPEN".equals(groupBuy.getStatus())) {
            // Check deadline proximity
            long hoursLeft = LocalDateTime.now().until(groupBuy.getDeadlineDateTime(), ChronoUnit.HOURS);
            int membersNeeded = groupBuy.getMaxMembers() - groupBuy.getCurrentMemberCount();
            
            if (hoursLeft <= 24 && membersNeeded > 0) {
                return String.format("%d hours left to lock the group. %d more people needed.", 
                                     hoursLeft, membersNeeded);
            }
        }
        return null;
    }
}
