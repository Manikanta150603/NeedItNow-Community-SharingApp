package com.needitnow.services;

import com.needitnow.entity.Community;
import com.needitnow.entity.Task;
import com.needitnow.entity.TaskOffer;
import com.needitnow.entity.User;
import com.needitnow.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    public Task createTask(String title, String description, LocalDate dueDate, String timeNeeded, 
                     String category, String priority, String location, String estimatedDuration, 
                     String reward, boolean hasReward, String imagePath, 
                     User creator, Community community) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setDueDate(dueDate);
        task.setTimeNeeded(timeNeeded);
        task.setCategory(category);
        task.setPriority(priority);
        task.setLocation(location);
        task.setEstimatedDuration(estimatedDuration);
        task.setReward(reward);
        task.setHasReward(hasReward);
        task.setImagePath(imagePath);
        task.setCreator(creator);
        task.setCommunity(community);
        task.setCompleted(false);
        return taskRepository.save(task);
    }

    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId).orElse(null);
    }

    public List<Task> getOpenTasksInCommunity(Community community) {
        System.out.println("TaskService: Fetching open tasks for community ID: " + (community != null ? community.getId() : "null"));
        
        if (community == null) {
            System.out.println("TaskService: Community is null, returning empty list");
            return new ArrayList<>();
        }
        
        List<Task> tasks = taskRepository.findByCommunityAndCompletedFalseAndHelperIsNull(community);
        System.out.println("TaskService: Fetched " + (tasks != null ? tasks.size() : 0) + " open tasks for community " + community.getId());
        
        if (tasks != null) {
            System.out.println("TaskService: Task details:");
            for (Task task : tasks) {
                System.out.println("  - " + task.getTitle() + " (ID: " + task.getId() + ", Creator: " + 
                               (task.getCreator() != null ? task.getCreator().getUsername() : "null") + ")");
            }
        } else {
            System.out.println("TaskService: No tasks found or error occurred");
        }
        
        return tasks != null ? tasks : new ArrayList<>();
    }

    public List<Task> getHighPriorityOpenTasksInCommunity(Community community) {
        List<Task> tasks = taskRepository.findByCommunityAndPriorityAndCompletedFalseAndHelperIsNull(community, "HIGH");
        System.out.println("TaskService: Fetched " + (tasks != null ? tasks.size() : 0) + " high-priority open tasks for community " + community.getId());
        return tasks != null ? tasks : new ArrayList<>();
    }

    public List<Task> getUserTasksInCommunity(Community community, User user) {
        List<Task> tasks = taskRepository.findByCommunityAndCreator(community, user);
        System.out.println("TaskService: Fetched " + (tasks != null ? tasks.size() : 0) + " user tasks for user " + user.getId() + " in community " + community.getId());
        return tasks != null ? tasks : new ArrayList<>();
    }

    public List<Task> getHelpingTasksInCommunity(Community community, User user) {
        List<Task> tasks = taskRepository.findByCommunityAndHelper(community, user);
        System.out.println("TaskService: Fetched " + (tasks != null ? tasks.size() : 0) + " helping tasks for user " + user.getId() + " in community " + community.getId());
        return tasks != null ? tasks : new ArrayList<>();
    }

    public List<Task> getCompletedTasksInCommunity(Community community) {
        List<Task> tasks = taskRepository.findByCommunityAndCompletedTrue(community);
        System.out.println("TaskService: Fetched " + (tasks != null ? tasks.size() : 0) + " completed tasks for community " + community.getId());
        return tasks != null ? tasks : new ArrayList<>();
    }

    public TaskOffer offerHelp(Task task, User user, String message) {
        if (task.isCompleted() || task.getHelper() != null) {
            return null; // Cannot offer help if task is completed or has a helper
        }
        TaskOffer offer = new TaskOffer();
        offer.setTask(task);
        offer.setUser(user);
        offer.setMessage(message);
        offer.setStatus("pending");
        offer.setCreatedAt(LocalDateTime.now());
        task.getOffers().add(offer);
        taskRepository.save(task); // Cascade saves the offer
        return offer;
    }

    public boolean acceptOffer(Task task, Long offerId) {
        if (task.isCompleted() || task.getHelper() != null) {
            return false;
        }
        TaskOffer offer = task.getOffers().stream()
                .filter(o -> o.getId().equals(offerId))
                .findFirst()
                .orElse(null);
        if (offer == null) {
            return false;
        }
        task.setHelper(offer.getUser());
        task.setHelperNote(offer.getMessage());
        offer.setStatus("accepted");
        // Optionally reject other offers
        task.getOffers().stream()
                .filter(o -> !o.getId().equals(offerId))
                .forEach(o -> o.setStatus("rejected"));
        taskRepository.save(task);
        return true;
    }

    @Transactional(readOnly = true)
    public List<TaskOffer> getOffersForTask(Long taskId) {
        // Fetch the task with its offers and user details in a single query
        Optional<Task> taskOpt = taskRepository.findByIdWithOffersAndUsers(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            // Initialize the lazy collections
            if (task.getOffers() != null) {
                // Ensure the task is set on each offer for the PostLoad method
                for (TaskOffer offer : task.getOffers()) {
                    offer.setTask(task);
                }
                System.out.println("Offers for task " + taskId + ": " + task.getOffers().size() + " offers found");
                return task.getOffers();
            }
            return new ArrayList<>();
        }
        System.out.println("No task found for ID " + taskId);
        return new ArrayList<>();
    }

    public boolean completeTask(Task task, String feedback) {
        if (task.isCompleted()) {
            return false;
        }
        task.setCompleted(true);
        task.setCompletedDate(LocalDateTime.now());
        task.setFeedback(feedback);
        taskRepository.save(task);
        return true;
    }

    public boolean deleteTask(Task task) {
        if (task.getHelper() != null) {
            return false;
        }
        taskRepository.delete(task);
        return true;
    }
    
    
    
   

    
    public TaskOffer getOfferById(Long offerId) {
        return taskRepository.findOfferById(offerId).orElse(null);
    }

    public void cancelOffer(TaskOffer offer) {
        offer.setStatus("cancelled");
        taskRepository.save(offer.getTask()); // Cascade saves the offer
    }
    
    @Transactional(readOnly = true)
    public List<Task> getCompletedTasksForUserInCommunity(Community community, User user) {
        return taskRepository.findCompletedTasksForUserInCommunity(community, user);
    }
    
    @Transactional(readOnly = true)
    public List<Task> getCancelledTasksForUserInCommunity(Community community, User user) {
        return taskRepository.findCancelledTasksForUserInCommunity(community, user);
    }

    public List<TaskOffer> getUserOffersInCommunity(Community community, User user) {
        List<Task> tasks = taskRepository.findByCommunity(community);
        List<TaskOffer> offers = new ArrayList<>();
        for (Task task : tasks) {
            offers.addAll(task.getOffers().stream()
                    .filter(offer -> offer.getUser().getId().equals(user.getId()))
                    .collect(Collectors.toList()));
        }
        System.out.println("TaskService: Fetched " + offers.size() + " offers for user " + user.getId() + " in community " + community.getId());
        return offers;
    }

    
}