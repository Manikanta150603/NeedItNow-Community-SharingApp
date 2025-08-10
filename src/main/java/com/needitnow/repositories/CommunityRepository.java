package com.needitnow.repositories;

import com.needitnow.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    
    @Query("SELECT c FROM Community c WHERE " +
           "LOWER(c.communityName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.state) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "c.pincode LIKE CONCAT('%', :query, '%')")
    List<Community> searchCommunities(@Param("query") String query);
    
    @Query(value = "SELECT *, (6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(latitude)))) AS distance " +
            "FROM communities WHERE latitude IS NOT NULL AND longitude IS NOT NULL " +
            "HAVING distance < :radius ORDER BY distance", nativeQuery = true)
    List<Community> findNearbyCommunities(@Param("latitude") double latitude, 
                                         @Param("longitude") double longitude, 
                                         @Param("radius") double radius);
List<Community> findByUserId(Long userId);
    
    boolean existsByIdAndUserId(Long id, Long userId);

@Query("SELECT c FROM Community c " +
        "WHERE c.id IN (SELECT r.community.id FROM CommunityJoinRequest r " +
        "WHERE r.user.id = :userId AND r.status = 'APPROVED') " +
        "OR c.user.id = :userId")
 List<Community> findCommunitiesByMemberId(@Param("userId") Long userId);

@Query("SELECT c, COUNT(r.user) as memberCount FROM Community c " +
	       "LEFT JOIN CommunityJoinRequest r ON r.community.id = c.id AND r.status = 'APPROVED' " +
	       "WHERE r.user.id = :userId OR c.user.id = :userId " +
	       "GROUP BY c.id")
	List<Object[]> findCommunitiesWithMemberCountByUserId(@Param("userId") Long userId);
}


