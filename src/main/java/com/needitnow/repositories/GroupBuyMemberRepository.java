package com.needitnow.repositories;

import com.needitnow.entity.GroupBuy;
import com.needitnow.entity.GroupBuyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBuyMemberRepository extends JpaRepository<GroupBuyMember, Long> {
    List<GroupBuyMember> findByGroupBuyId(Long groupBuyId);
    List<GroupBuyMember> findByGroupBuyIdAndActive(Long groupBuyId, boolean active);
    List<GroupBuyMember> findByUserId(Long userId);
    List<GroupBuyMember> findByUserIdAndActive(Long userId, boolean active);
    List<GroupBuyMember> findByGroupBuyIdAndUserId(Long groupBuyId, Long userId);
    List<GroupBuyMember> findByGroupBuyIdAndUserIdAndActive(Long groupBuyId, Long userId, boolean active);
    
    @Query("SELECT gbm.groupBuy FROM GroupBuyMember gbm WHERE gbm.user.id = :userId AND gbm.isCreator = false ORDER BY gbm.joinedAt DESC")
    List<GroupBuy> findGroupBuysByUserIdAndIsCreatorFalse(@Param("userId") Long userId);
    
    @Query("SELECT gbm.groupBuy FROM GroupBuyMember gbm WHERE gbm.user.id = :userId AND gbm.isCreator = false AND gbm.groupBuy.status = :status ORDER BY gbm.joinedAt DESC")
    List<GroupBuy> findGroupBuysByUserIdAndIsCreatorFalseAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    @Query("SELECT gbm FROM GroupBuyMember gbm WHERE gbm.groupBuy.id = :groupBuyId ORDER BY gbm.isCreator DESC, gbm.joinedAt ASC")
    List<GroupBuyMember> findByGroupBuyIdOrderByCreatorAndJoinTime(@Param("groupBuyId") Long groupBuyId);
    
    @Query("SELECT COUNT(gbm) FROM GroupBuyMember gbm WHERE gbm.groupBuy.id = :groupBuyId AND gbm.received = true")
    long countReceivedMembersByGroupBuyId(@Param("groupBuyId") Long groupBuyId);
}
