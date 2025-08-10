package com.needitnow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    

    public ChatMessage() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ChatMessage(Long id, Item item, User sender, User receiver, String message, LocalDateTime timestamp) {
		super();
		this.id = id;
		this.item = item;
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
		this.timestamp = timestamp;
	}
	// Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
	@Override
	public String toString() {
		return "ChatMessage [id=" + id + ", item=" + item + ", sender=" + sender + ", receiver=" + receiver
				+ ", message=" + message + ", timestamp=" + timestamp + ", getId()=" + getId() + ", getItem()="
				+ getItem() + ", getSender()=" + getSender() + ", getReceiver()=" + getReceiver() + ", getMessage()="
				+ getMessage() + ", getTimestamp()=" + getTimestamp() + ", getClass()=" + getClass() + ", hashCode()="
				+ hashCode() + ", toString()=" + super.toString() + "]";
	}
    
    
}