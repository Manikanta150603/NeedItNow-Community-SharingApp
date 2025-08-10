package com.needitnow.repositories;

import com.needitnow.entity.CommunityJoinRequest;
import com.needitnow.entity.Community;
import com.needitnow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface CommunityJoinRequestRepository extends JpaRepository<CommunityJoinRequest, Long> {
    List<CommunityJoinRequest> findByCommunityAndUser(Community community, User user);
    List<CommunityJoinRequest> findByCommunity(Community community);
    List<CommunityJoinRequest> findByUser(User user);
    List<CommunityJoinRequest> findByCommunity_User(User admin);
    
    @Query("SELECT r FROM CommunityJoinRequest r " +
    	       "JOIN FETCH r.community c " +
    	       "JOIN FETCH r.user u " +
    	       "WHERE c.user.id = :adminId AND r.status = 'PENDING'")
    	List<CommunityJoinRequest> findPendingRequestsForAdmin(@Param("adminId") Long adminId);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM CommunityJoinRequest r WHERE r.community.id = :communityId AND r.user.id = :userId AND r.status IN :statuses")
    boolean existsByCommunityIdAndUserIdAndStatusIn(
        @Param("communityId") Long communityId,
        @Param("userId") Long userId,
        @Param("statuses") List<String> statuses
    );

    @Query("SELECT COUNT(r) FROM CommunityJoinRequest r " +
    	       "WHERE r.community.user.id = :adminId AND r.status = 'PENDING'")
    int countPendingRequestsForAdmin(@Param("adminId") Long adminId);
    
    @Query("SELECT c.id, COUNT(r.user) + 1 " + // +1 for creator
    	       "FROM Community c " +
    	       "LEFT JOIN CommunityJoinRequest r ON r.community.id = c.id AND r.status = 'APPROVED' " +
    	       "WHERE c.id IN :communityIds " +
    	       "GROUP BY c.id")
    	List<Object[]> findMemberCountsForCommunities(@Param("communityIds") List<Long> communityIds);

    	// Renamed helper method
    	default Map<Long, Long> getMemberCountsMap(List<Long> communityIds) {
    	    return findMemberCountsForCommunities(communityIds).stream()
    	        .collect(Collectors.toMap(
    	            arr -> (Long) arr[0],
    	            arr -> (Long) arr[1]
    	        ));
    	}
    List<CommunityJoinRequest> findByUserAndStatus(User user, String status);
    int countByUserAndStatus(User user, String status);
    
    @Query("SELECT COUNT(r) > 0 FROM CommunityJoinRequest r WHERE r.user.id = :userId AND r.community.id = :communityId AND r.status = :status")
    boolean existsByUserIdAndCommunityIdAndStatus(
        @Param("userId") Long userId,
        @Param("communityId") Long communityId,
        @Param("status") String status
    );
    
    @Query("SELECT COUNT(r) FROM CommunityJoinRequest r WHERE r.community.id = :communityId AND r.status = :status")
    long countByCommunityIdAndStatus(
        @Param("communityId") Long communityId,
        @Param("status") String status
    );
    
}
