package com.needitnow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String category;
    private int availableQuantity;
    private LocalDate dateOfPurchase;
    private String itemName;
    private BigDecimal price;
    private boolean isFree;
    private Integer autoRemoveAfter;
    private String autoRemoveUnit;
    private String additionalNotes;
    @Lob
    private byte[] photoData;
    private boolean displayName;
    private boolean allowPhone;
    private LocalDateTime dateOfPosting;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester;

    private boolean requestAccepted = false;
    private boolean requestRejected = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_requester_id")
    private User lastRequester; // To keep track of who made the request even after rejection
    
    private boolean requestItem = false; // Flag to indicate if this is a request from a neighbor
    private String urgency; // Urgency level for neighbor requests

    public Item(Long id, Community community, User user, String category, int availableQuantity,
			LocalDate dateOfPurchase, String itemName, BigDecimal price, boolean isFree, Integer autoRemoveAfter,
			String autoRemoveUnit, String additionalNotes, byte[] photoData, boolean displayName, boolean allowPhone,
			LocalDateTime dateOfPosting, User requester, boolean requestAccepted) {
		super();
		this.id = id;
		this.community = community;
		this.user = user;
		this.category = category;
		this.availableQuantity = availableQuantity;
		this.dateOfPurchase = dateOfPurchase;
		this.itemName = itemName;
		this.price = price;
		this.isFree = isFree;
		this.autoRemoveAfter = autoRemoveAfter;
		this.autoRemoveUnit = autoRemoveUnit;
		this.additionalNotes = additionalNotes;
		this.photoData = photoData;
		this.displayName = displayName;
		this.allowPhone = allowPhone;
		this.dateOfPosting = dateOfPosting;
		this.requester = requester;
	}

	@Transient
    public String getPhotoDataBase64() {
        if (photoData != null) {
            return Base64.getEncoder().encodeToString(photoData);
        }
        return null;
    }
	
	

    public Item() {
		super();
		// TODO Auto-generated constructor stub
	}

	public User getRequester() {
		return requester;
	}

	public void setRequester(User requester) {
		this.requester = requester;
	}

	public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Community getCommunity() { return community; }
    public void setCommunity(Community community) { this.community = community; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public LocalDate getDateOfPurchase() { return dateOfPurchase; }
    public void setDateOfPurchase(LocalDate dateOfPurchase) { this.dateOfPurchase = dateOfPurchase; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public boolean isFree() { return isFree; }
    public void setFree(boolean free) { isFree = free; }

    public Integer getAutoRemoveAfter() { return autoRemoveAfter; }
    public void setAutoRemoveAfter(Integer autoRemoveAfter) { this.autoRemoveAfter = autoRemoveAfter; }

    public String getAutoRemoveUnit() { return autoRemoveUnit; }
    public void setAutoRemoveUnit(String autoRemoveUnit) { this.autoRemoveUnit = autoRemoveUnit; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public byte[] getPhotoData() { return photoData; }
    public void setPhotoData(byte[] photoData) { this.photoData = photoData; }

    public boolean isDisplayName() { return displayName; }
    public void setDisplayName(boolean displayName) { this.displayName = displayName; }

    public boolean isAllowPhone() { return allowPhone; }
    public void setAllowPhone(boolean allowPhone) { this.allowPhone = allowPhone; }

    public LocalDateTime getDateOfPosting() { return dateOfPosting; }
    public void setDateOfPosting(LocalDateTime dateOfPosting) { this.dateOfPosting = dateOfPosting; }

    public boolean isRequestAccepted() { return requestAccepted; }
    public void setRequestAccepted(boolean requestAccepted) { this.requestAccepted = requestAccepted; }
    
    public boolean isRequestRejected() { return requestRejected; }
    public void setRequestRejected(boolean requestRejected) { this.requestRejected = requestRejected; }
    
    public boolean isRequestItem() { return requestItem; }
    public void setRequestItem(boolean requestItem) { this.requestItem = requestItem; }
    
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    
    public User getLastRequester() { return lastRequester; }
    public void setLastRequester(User lastRequester) { this.lastRequester = lastRequester; }
}