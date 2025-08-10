package com.needitnow.repositories;

import com.needitnow.entity.Community;
import com.needitnow.entity.Task;
import com.needitnow.entity.TaskOffer;
import com.needitnow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCommunity(Community community);
    @Query("SELECT t FROM Task t JOIN FETCH t.creator WHERE t.community = :community AND t.completed = false AND t.helper IS NULL")
    List<Task> findByCommunityAndCompletedFalseAndHelperIsNull(@Param("community") Community community);
    List<Task> findByCommunityAndCreator(Community community, User creator);
    List<Task> findByCommunityAndHelper(Community community, User helper);
    List<Task> findByCommunityAndCompletedTrue(Community community);
    List<Task> findByCreatorAndCompletedTrue(User creator);
    List<Task> findByHelperAndCompletedTrue(User helper);
    @Query("SELECT t FROM Task t JOIN FETCH t.creator WHERE t.community = :community AND t.priority = :priority AND t.completed = false AND t.helper IS NULL")
    List<Task> findByCommunityAndPriorityAndCompletedFalseAndHelperIsNull(@Param("community") Community community, @Param("priority") String priority);
    
    // Custom query to fetch a task with its offers and user details
    @Query("SELECT DISTINCT t FROM Task t " +
           "LEFT JOIN FETCH t.offers o " +
           "LEFT JOIN FETCH o.user u " +
           "WHERE t.id = :taskId")
    Optional<Task> findByIdWithOffersAndUsers(@Param("taskId") Long taskId);
    
    
    
    @Query("SELECT o FROM TaskOffer o WHERE o.id = :offerId")
    Optional<TaskOffer> findOfferById(@Param("offerId") Long offerId);
    
    // Find completed tasks where user is either creator or helper
    @Query("SELECT t FROM Task t WHERE t.community = :community AND t.completed = true AND (t.creator = :user OR t.helper = :user) ORDER BY t.completedDate DESC")
    List<Task> findCompletedTasksForUserInCommunity(@Param("community") Community community, @Param("user") User user);
    
    // Find cancelled tasks (tasks with cancelled offers) where user is either creator or helper
    @Query("SELECT DISTINCT t FROM Task t " +
           "LEFT JOIN t.offers o " +
           "WHERE t.community = :community " +
           "AND (t.creator = :user OR t.helper = :user) " +
           "AND (o.status = 'cancelled' OR o.status = 'rejected') " +
           "ORDER BY t.updatedAt DESC")
    List<Task> findCancelledTasksForUserInCommunity(@Param("community") Community community, @Param("user") User user);
}