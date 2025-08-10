package com.needitnow.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.util.Base64;

@Entity
@Table(name = "neighbor_requests")
public class NeighborRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester; // The person asking for help
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acceptor_id")
    private User acceptor; // The person who accepted the request
    
    private String title;
    private String category;
    private String description;
    private String urgency; // Normal, Urgent, Very Urgent
    
    @Lob
    private byte[] photoData;
    
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt; // When the request was accepted
    private LocalDateTime resolvedAt;
    private boolean isResolved;
    private boolean displayName; // Whether to show requester's name
    private boolean allowPhone; // Whether to allow phone contact
    
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true; // Whether the request is active
    
    public NeighborRequest() {
    }
    
    @Transient
    public String getPhotoDataBase64() {
        if (photoData != null) {
            return Base64.getEncoder().encodeToString(photoData);
        }
        return null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public byte[] getPhotoData() {
        return photoData;
    }

    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }

    public boolean isDisplayName() {
        return displayName;
    }

    public void setDisplayName(boolean displayName) {
        this.displayName = displayName;
    }

    public boolean isAllowPhone() {
        return allowPhone;
    }

    public void setAllowPhone(boolean allowPhone) {
        this.allowPhone = allowPhone;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public User getAcceptor() {
        return acceptor;
    }
    
    public void setAcceptor(User acceptor) {
        this.acceptor = acceptor;
    }
    
    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }
    
    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
}
