package com.needitnow.services;

import com.needitnow.entity.GroupBuy;
import com.needitnow.entity.GroupBuyMember;
import com.needitnow.entity.User;
import com.needitnow.repositories.GroupBuyMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GroupBuyMemberService {

    @Autowired
    private GroupBuyMemberRepository groupBuyMemberRepository;

    public GroupBuyMember saveGroupBuyMember(GroupBuyMember groupBuyMember) {
        return groupBuyMemberRepository.save(groupBuyMember);
    }

    public Optional<GroupBuyMember> findById(Long id) {
        return groupBuyMemberRepository.findById(id);
    }

    public List<GroupBuyMember> findByGroupBuyId(Long groupBuyId) {
        return groupBuyMemberRepository.findByGroupBuyId(groupBuyId);
    }

    public List<GroupBuyMember> findActiveByGroupBuyId(Long groupBuyId) {
        return groupBuyMemberRepository.findByGroupBuyIdAndActive(groupBuyId, true);
    }

    public List<GroupBuyMember> findByUserId(Long userId) {
        return groupBuyMemberRepository.findByUserId(userId);
    }

    public List<GroupBuyMember> findActiveByUserId(Long userId) {
        return groupBuyMemberRepository.findByUserIdAndActive(userId, true);
    }

    public List<GroupBuyMember> findByGroupBuyIdAndUserId(Long groupBuyId, Long userId) {
        return groupBuyMemberRepository.findByGroupBuyIdAndUserId(groupBuyId, userId);
    }

    public List<GroupBuyMember> findActiveByGroupBuyIdAndUserId(Long groupBuyId, Long userId) {
        return groupBuyMemberRepository.findByGroupBuyIdAndUserIdAndActive(groupBuyId, userId, true);
    }
    
    public boolean isUserMemberOfGroupBuy(Long userId, Long groupBuyId) {
        List<GroupBuyMember> members = findByGroupBuyIdAndUserId(groupBuyId, userId);
        return !members.isEmpty();
    }
    
    public GroupBuyMember save(GroupBuyMember member) {
        return groupBuyMemberRepository.save(member);
    }

    public GroupBuyMember createMember(GroupBuy groupBuy, User user, int requestedQuantity) {
        GroupBuyMember member = new GroupBuyMember();
        member.setGroupBuy(groupBuy);
        member.setUser(user);
        member.setQuantity((double) requestedQuantity);
        member.setJoinedAt(LocalDateTime.now());
        member.setConfirmed(true); // Set as confirmed by default since the creator is automatically confirmed
        member.setCreator(true);   // Mark as the creator of the group buy
        
        return groupBuyMemberRepository.save(member);
    }

    public void deleteGroupBuyMember(Long id) {
        groupBuyMemberRepository.deleteById(id);
    }
    
    public List<GroupBuyMember> findMembersByGroupBuyIdSorted(Long groupBuyId) {
        return groupBuyMemberRepository.findByGroupBuyIdOrderByCreatorAndJoinTime(groupBuyId);
    }
    
    public boolean markItemAsReceived(Long memberId) {
        Optional<GroupBuyMember> optMember = groupBuyMemberRepository.findById(memberId);
        if (optMember.isPresent()) {
            GroupBuyMember member = optMember.get();
            member.setReceived(true); // This also sets receiptTime via setter
            groupBuyMemberRepository.save(member);
            return true;
        }
        return false;
    }
    
    public boolean leaveGroupBuy(Long groupBuyId, Long userId) {
        List<GroupBuyMember> members = findByGroupBuyIdAndUserId(groupBuyId, userId);
        if (!members.isEmpty()) {
            GroupBuyMember member = members.get(0);
            
            // Don't allow the creator to leave
            if (member.isCreator()) {
                return false;
            }
            
            member.setLeftGroup(true); // This also sets leftTime and reduces score via setter
            member.setActive(false);
            groupBuyMemberRepository.save(member);
            
            // Update the group buy's member count
            GroupBuy groupBuy = member.getGroupBuy();
            groupBuy.setCurrentMemberCount(groupBuy.getCurrentMemberCount() - 1);
            groupBuy.setCurrentQuantity(groupBuy.getCurrentQuantity() - member.getQuantity().intValue());
            
            return true;
        }
        return false;
    }
    
    public long getReceivedMemberCountForGroupBuy(Long groupBuyId) {
        return groupBuyMemberRepository.countReceivedMembersByGroupBuyId(groupBuyId);
    }
    
    public void updateMemberScore(Long memberId, int scoreChange) {
        Optional<GroupBuyMember> optMember = groupBuyMemberRepository.findById(memberId);
        if (optMember.isPresent()) {
            GroupBuyMember member = optMember.get();
            int newScore = member.getMemberScore() + scoreChange;
            // Keep score between 0 and 100
            newScore = Math.max(0, Math.min(100, newScore));
            member.setMemberScore(newScore);
            groupBuyMemberRepository.save(member);
        }
    }
}
