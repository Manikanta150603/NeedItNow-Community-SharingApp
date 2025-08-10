package com.needitnow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_join_requests")
public class CommunityJoinRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "status_updated_at")
    private LocalDateTime statusUpdatedAt;

    // Add to constructor and getters/setters

    public CommunityJoinRequest() {}

    

    public LocalDateTime getStatusUpdatedAt() {
		return statusUpdatedAt;
	}



	public void setStatusUpdatedAt(LocalDateTime statusUpdatedAt) {
		this.statusUpdatedAt = statusUpdatedAt;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public CommunityJoinRequest(Community community, User user, String status) {
        this.community = community;
        this.user = user;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.statusUpdatedAt = LocalDateTime.now();
    }

    // Keep the full constructor (optional)
    public CommunityJoinRequest(Long id, Community community, User user, String status, 
                              LocalDateTime createdAt, LocalDateTime statusUpdatedAt) {
        this.id = id;
        this.community = community;
        this.user = user;
        this.status = status;
        this.createdAt = createdAt;
        this.statusUpdatedAt = statusUpdatedAt;
    }



	public Long getId() { return id; }
    public Community getCommunity() { return community; }
    public void setCommunity(Community community) { this.community = community; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
