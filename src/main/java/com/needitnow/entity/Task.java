package com.needitnow.entity;


import org.hibernate.annotations.CreationTimestamp;
import java.util.*;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {
    public Task() {}

    // Getters and setters will be added for all fields below

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "time_needed")
    private String timeNeeded;

    @Column(name = "category")
    private String category;

    @Column(name = "priority")
    private String priority;

    @Column(name = "location")
    private String location;

    @Column(name = "estimated_duration")
    private String estimatedDuration;

    @Column(name = "reward")
    private String reward;

    @Column(name = "has_reward")
    private boolean hasReward = false;

    @Column(name = "image_path")
    private String imagePath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "helper_id")
    private User helper;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Column(name = "completed", nullable = false, columnDefinition = "boolean default false")
    private boolean completed = false;

    private LocalDateTime completedDate;

    @Column(name = "feedback")
    private String feedback;

    // Note for helper when offering help
    private String helperNote;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskOffer> offers = new ArrayList<>();

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }
    public User getHelper() { return helper; }
    public void setHelper(User helper) { this.helper = helper; }
    public Community getCommunity() { return community; }
    public void setCommunity(Community community) { this.community = community; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public String getHelperNote() { return helperNote; }
    public void setHelperNote(String helperNote) { this.helperNote = helperNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getTimeNeeded() { return timeNeeded; }
    public void setTimeNeeded(String timeNeeded) { this.timeNeeded = timeNeeded; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(String estimatedDuration) { this.estimatedDuration = estimatedDuration; }
    
    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }
    
    public boolean isHasReward() { return hasReward; }
    public void setHasReward(boolean hasReward) { this.hasReward = hasReward; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
    public List<TaskOffer> getOffers() { return offers; }
    public void setOffers(List<TaskOffer> offers) { this.offers = offers; }
}

