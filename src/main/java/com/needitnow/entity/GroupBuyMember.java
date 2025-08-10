package com.needitnow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_buy_members")
public class GroupBuyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_buy_id", nullable = false)
    private GroupBuy groupBuy;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "requested_quantity", nullable = false)
    private int requestedQuantity;
    
    @Column(name = "quantity", nullable = false)
    private Double quantityValue;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "confirmed")
    private boolean confirmed;

    @Column(nullable = false)
    private boolean paid = false;

    @Column(nullable = false)
    private boolean isCreator = false;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(nullable = false)
    private boolean received = false;
    
    @Column(nullable = false)
    private boolean leftGroup = false;
    
    @Column(name = "member_score", nullable = false)
    private Integer memberScore = 100; // Default score for members
    
    @Column(name = "receipt_time")
    private LocalDateTime receiptTime;
    
    @Column(name = "left_time")
    private LocalDateTime leftTime;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GroupBuy getGroupBuy() {
        return groupBuy;
    }

    public void setGroupBuy(GroupBuy groupBuy) {
        this.groupBuy = groupBuy;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getQuantity() { 
        return quantityValue; 
    }
    
    public void setQuantity(Double quantity) { 
        this.quantityValue = quantity;
        this.requestedQuantity = quantity != null ? quantity.intValue() : 0;
    }
    
    public int getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public void setRequestedQuantity(int requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
        this.quantityValue = (double) requestedQuantity;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isCreator() {
        return isCreator;
    }

    public void setCreator(boolean creator) {
        isCreator = creator;
    }
    
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isReceived() {
        return received;
    }
    
    public void setReceived(boolean received) {
        this.received = received;
        if (received) {
            this.receiptTime = LocalDateTime.now();
        }
    }
    
    public boolean isLeftGroup() {
        return leftGroup;
    }
    
    public void setLeftGroup(boolean leftGroup) {
        this.leftGroup = leftGroup;
        if (leftGroup) {
            this.leftTime = LocalDateTime.now();
            this.memberScore -= 10; // Decrease score when leaving group
        }
    }
    
    public Integer getMemberScore() {
        return memberScore != null ? memberScore : 100;
    }
    
    public void setMemberScore(Integer memberScore) {
        this.memberScore = memberScore != null ? memberScore : 100;
    }
    
    public LocalDateTime getReceiptTime() {
        return receiptTime;
    }
    
    public void setReceiptTime(LocalDateTime receiptTime) {
        this.receiptTime = receiptTime;
    }
    
    public LocalDateTime getLeftTime() {
        return leftTime;
    }
    
    public void setLeftTime(LocalDateTime leftTime) {
        this.leftTime = leftTime;
    }
}
