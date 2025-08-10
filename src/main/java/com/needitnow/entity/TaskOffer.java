package com.needitnow.entity;

import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "task_offers")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "offers"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "tasks", "offers"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "status", nullable = false)
    private String status = "pending";
    
    @Transient
    @JsonProperty("taskCompleted")
    private Boolean taskCompleted = false;
    
    @PostLoad
    private void setTaskCompleted() {
        if (task != null) {
            this.taskCompleted = task.isCompleted();
        }
    }
    
    @JsonAnyGetter
    public Map<String, Object> getTaskStatus() {
        Map<String, Object> map = new HashMap<>();
        if (task != null) {
            map.put("taskCompleted", task.isCompleted());
            map.put("taskId", task.getId());
        }
        return map;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}