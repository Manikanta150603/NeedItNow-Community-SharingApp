package com.needitnow.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "communities")
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String communityName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String pincode;

    @Column(nullable = false)
    private String country;

    private Double latitude;
    private Double longitude;
    
    

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Association with User
    
    @Transient // This field won't be persisted in the database
    private Long memberCount;

    // Add getter and setter
    public Long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Long memberCount) {
        this.memberCount = memberCount;
    }
    
    

    public Community() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    
	public Community(Long id, String communityName, String address, String city, String state, String pincode,
			String country, Double latitude, Double longitude, User user) {
		super();
		this.id = id;
		this.communityName = communityName;
		this.address = address;
		this.city = city;
		this.state = state;
		this.pincode = pincode;
		this.country = country;
		this.latitude = latitude;
		this.longitude = longitude;
		this.user = user;
	}


	// Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCommunityName() { return communityName; }
    public void setCommunityName(String communityName) { this.communityName = communityName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
