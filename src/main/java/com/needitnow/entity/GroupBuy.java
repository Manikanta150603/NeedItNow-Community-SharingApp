package com.needitnow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Base64;

@Entity
@Table(name = "group_buys")
public class GroupBuy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private int maxMembers;

    @Column(nullable = false)
    private int maxQuantity;

    @Column(nullable = false)
    private int minQuantityNeeded;

    @Column(nullable = false)
    private String quantityUnit;

    @Column(nullable = false)
    private BigDecimal pricePerUnit;

    @Column(nullable = false)
    private LocalDateTime deadlineDateTime;

    @Column(nullable = false)
    private LocalDateTime plannedPurchaseTime;

    private String pickupWindow;

    @Column(nullable = false)
    private String deliveryPoint;

    @Column(nullable = false)
    private boolean autoConfirmMembers = true;

    @Column(nullable = false)
    private boolean confirmationRequired = false;

    @Column(nullable = false)
    private boolean allowCancellation = false;

    @Column(columnDefinition = "TEXT")
    private String notesToMembers;

    @Lob
    private byte[] photoData;

    @Column(nullable = false)
    private boolean displayName = true;

    @Column(nullable = false)
    private boolean allowPhone = false;

    @Column(nullable = false)
    private boolean onlyAutoConfirmed = false;

    @Column(nullable = false)
    private boolean advanceContribution = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean active = true;
    
    @Column(nullable = false)
    private String status = "OPEN"; // OPEN, LOCKED, COMPLETED, CANCELLED
    
    @Column(columnDefinition = "TEXT")
    private String cancellationReason;
    
    @Column(nullable = false)
    private boolean locked = false;

    @Column(nullable = false)
    private int currentMemberCount = 1; // Creator is the first member

    @Column(nullable = false)
    private int currentQuantity = 0;
    
    @OneToMany(mappedBy = "groupBuy", fetch = FetchType.LAZY)
    private List<GroupBuyMember> members;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public int getMinQuantityNeeded() {
        return minQuantityNeeded;
    }

    public void setMinQuantityNeeded(int minQuantityNeeded) {
        this.minQuantityNeeded = minQuantityNeeded;
    }

    public String getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public LocalDateTime getDeadlineDateTime() {
        return deadlineDateTime;
    }

    public void setDeadlineDateTime(LocalDateTime deadlineDateTime) {
        this.deadlineDateTime = deadlineDateTime;
    }

    public LocalDateTime getPlannedPurchaseTime() {
        return plannedPurchaseTime;
    }

    public void setPlannedPurchaseTime(LocalDateTime plannedPurchaseTime) {
        this.plannedPurchaseTime = plannedPurchaseTime;
    }

    public String getPickupWindow() {
        return pickupWindow;
    }

    public void setPickupWindow(String pickupWindow) {
        this.pickupWindow = pickupWindow;
    }

    public String getDeliveryPoint() {
        return deliveryPoint;
    }

    public void setDeliveryPoint(String deliveryPoint) {
        this.deliveryPoint = deliveryPoint;
    }

    public boolean isAutoConfirmMembers() {
        return autoConfirmMembers;
    }

    public void setAutoConfirmMembers(boolean autoConfirmMembers) {
        this.autoConfirmMembers = autoConfirmMembers;
    }

    public boolean isConfirmationRequired() {
        return confirmationRequired;
    }

    public void setConfirmationRequired(boolean confirmationRequired) {
        this.confirmationRequired = confirmationRequired;
    }

    public boolean isAllowCancellation() {
        return allowCancellation;
    }

    public void setAllowCancellation(boolean allowCancellation) {
        this.allowCancellation = allowCancellation;
    }

    public String getNotesToMembers() {
        return notesToMembers;
    }

    public void setNotesToMembers(String notesToMembers) {
        this.notesToMembers = notesToMembers;
    }

    public byte[] getPhotoData() {
        return photoData;
    }

    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
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

    public boolean isOnlyAutoConfirmed() {
        return onlyAutoConfirmed;
    }

    public void setOnlyAutoConfirmed(boolean onlyAutoConfirmed) {
        this.onlyAutoConfirmed = onlyAutoConfirmed;
    }

    public boolean isAdvanceContribution() {
        return advanceContribution;
    }

    public void setAdvanceContribution(boolean advanceContribution) {
        this.advanceContribution = advanceContribution;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    public boolean isLocked() {
        return locked;
    }
    
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getCurrentMemberCount() {
        return currentMemberCount;
    }

    public void setCurrentMemberCount(int currentMemberCount) {
        this.currentMemberCount = currentMemberCount;
    }

    public int getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(int currentQuantity) {
        this.currentQuantity = currentQuantity;
    }
    
    public List<GroupBuyMember> getMembers() {
        return members;
    }
    
    public void setMembers(List<GroupBuyMember> members) {
        this.members = members;
    }
    
    @Transient
    public String getPhotoDataBase64() {
        if (photoData != null) {
            return Base64.getEncoder().encodeToString(photoData);
        }
        return null;
    }
    
    // Helper methods for the UI
    @Transient
    public int getCurrentMembers() {
        return currentMemberCount;
    }
    
    @Transient
    public BigDecimal getPrice() {
        return pricePerUnit;
    }
    
    @Transient
    public LocalDateTime getEndDate() {
        return deadlineDateTime;
    }
    
    @Transient
    public User getOrganizer() {
        return this.user;
    }
    
    @Transient
    public String getOrganizerName() {
        return user != null ? user.getFullName() : "Unknown";
    }
}
