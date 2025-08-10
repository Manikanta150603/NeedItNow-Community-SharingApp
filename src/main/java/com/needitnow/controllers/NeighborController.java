package com.needitnow.controllers;

import com.needitnow.entity.Community;
import com.needitnow.entity.NeighborMessage;
import com.needitnow.entity.NeighborRequest;
import com.needitnow.entity.User;
import com.needitnow.services.CommunityService;
import com.needitnow.services.NeighborMessageService;
import com.needitnow.services.NeighborRequestService;
import com.needitnow.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user/community/{communityId}/neighbor")
public class NeighborController {
    
    @Autowired
    private NeighborRequestService neighborRequestService;
    
    @Autowired
    private NeighborMessageService neighborMessageService;
    
    @Autowired
    private CommunityService communityService;
    
    @Autowired
    private UserService userService;

    // View my requests (Ask Neighbor) - Maps to my-requests.html
    @GetMapping("/my-requests")
    public String viewMyRequests(
            @PathVariable Long communityId,
            HttpSession session,
            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        Community community = communityService.getCommunityById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
        
        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            return "redirect:/user/dashboard";
        }
        
        List<NeighborRequest> myRequestItems = neighborRequestService.getRequestsByUserInCommunity(user.getId(), communityId);
        
        model.addAttribute("community", community);
        model.addAttribute("communityId", communityId);
        model.addAttribute("myRequestItems", myRequestItems);
        model.addAttribute("currentUserId", user.getId());
        
        return "my-requests";
    }
    
    // Create a new request
    @PostMapping("/request")
    public String createRequest(
            @PathVariable Long communityId,
            @RequestParam("requestTitle") String title,
            @RequestParam("requestCategory") String category,
            @RequestParam("requestDescription") String description,
            @RequestParam("requestUrgency") String urgency,
            @RequestParam(value = "requestPhoto", required = false) MultipartFile photo,
            @RequestParam(value = "requestDuration", required = false) Integer duration,
            @RequestParam(value = "requestDurationUnit", required = false) String durationUnit,
            @RequestParam(value = "requestDisplayName", required = false, defaultValue = "false") boolean displayName,
            @RequestParam(value = "requestAllowPhone", required = false, defaultValue = "false") boolean allowPhone,
            HttpSession session,
            RedirectAttributes redirectAttributes) throws IOException {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        Community community = communityService.getCommunityById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
        
        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            return "redirect:/user/dashboard";
        }
        
        NeighborRequest request = new NeighborRequest();
        request.setCommunity(community);
        request.setRequester(user);
        request.setTitle(title);
        request.setCategory(category);
        request.setDescription(description);
        request.setUrgency(urgency);
        request.setDisplayName(displayName);
        request.setAllowPhone(allowPhone);
        request.setCreatedAt(LocalDateTime.now());
        
        if (photo != null && !photo.isEmpty()) {
            request.setPhotoData(photo.getBytes());
        }
        
        neighborRequestService.saveRequest(request);
        
        redirectAttributes.addFlashAttribute("successMessage", "Your request has been posted! Neighbors will be notified.");
        return "redirect:/user/community/" + communityId + "/items/my-requests";
    }
    
    // View neighbor requests (Help a Neighbor) - Maps to neighbor-requests.html
    @GetMapping("/help-requests")
    public String viewHelpRequests(
            @PathVariable Long communityId,
            HttpSession session,
            Model model) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        Community community = communityService.getCommunityById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
        
        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            return "redirect:/user/dashboard";
        }
        
        // Get requests from others in the community
        List<NeighborRequest> requestItems = neighborRequestService.getOtherRequestsInCommunity(communityId, user.getId());
        
        model.addAttribute("community", community);
        model.addAttribute("communityId", communityId);
        model.addAttribute("requestItems", requestItems);
        model.addAttribute("currentUserId", user.getId());
        
        return "neighbor-requests";
    }
    
    // View request details (both for requester and responder)
    @GetMapping("/request/{requestId}")
    public String viewRequestDetails(
            @PathVariable Long communityId,
            @PathVariable Long requestId,
            HttpSession session,
            Model model) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        NeighborRequest request = neighborRequestService.getRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        Community community = request.getCommunity();
        
        if (!community.getId().equals(communityId) || 
            !communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            return "redirect:/user/dashboard";
        }
        
        // Get all messages related to this request for this user
        List<NeighborMessage> messages = neighborMessageService.getUserMessagesForRequest(requestId, user.getId());
        
        // Map of user IDs to names for display in UI
        Map<Long, String> usernames = new HashMap<>();
        
        // Add requester's name
        Long requesterId = request.getRequester().getId();
        String requesterName = request.getRequester().getFullName();
        usernames.put(requesterId, requesterName);
        
        // Add current user's name
        usernames.put(user.getId(), user.getFullName());
        
        // Safely add other users from messages
        messages.forEach(msg -> {
            User sender = msg.getSender();
            User receiver = msg.getReceiver();
            
            if (sender != null && sender.getId() != null) {
                if (!usernames.containsKey(sender.getId())) {
                    try {
                        String senderName = sender.getFullName();
                        usernames.put(sender.getId(), senderName);
                    } catch (Exception e) {
                        // Fallback if we can't access the name due to lazy loading
                        User fullSender = userService.findById(sender.getId())
                                .orElse(null);
                        if (fullSender != null) {
                            usernames.put(fullSender.getId(), fullSender.getFullName());
                        }
                    }
                }
            }
            
            if (receiver != null && receiver.getId() != null) {
                if (!usernames.containsKey(receiver.getId())) {
                    try {
                        String receiverName = receiver.getFullName();
                        usernames.put(receiver.getId(), receiverName);
                    } catch (Exception e) {
                        // Fallback if we can't access the name due to lazy loading
                        User fullReceiver = userService.findById(receiver.getId())
                                .orElse(null);
                        if (fullReceiver != null) {
                            usernames.put(fullReceiver.getId(), fullReceiver.getFullName());
                        }
                    }
                }
            }
        });
        
        // Mark messages as read if user is receiver
        neighborMessageService.markMessagesAsRead(requestId, user.getId());
        
        model.addAttribute("request", request);
        model.addAttribute("messages", messages);
        model.addAttribute("community", community);
        model.addAttribute("communityId", communityId);
        model.addAttribute("currentUserId", user.getId());
        model.addAttribute("usernames", usernames);
        model.addAttribute("isRequester", request.getRequester().getId().equals(user.getId()));
        
        return "request-details";
    }
    
    // View responses to a request (for requester) - This will use existing modal in my-requests.html
    @GetMapping("/request/{requestId}/responses")
    @ResponseBody
    public ResponseEntity<?> getRequestResponses(
            @PathVariable Long communityId,
            @PathVariable Long requestId,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        
        NeighborRequest request = neighborRequestService.getRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
                
        // DEBUG MODE: Temporarily allow all users to access chat responses while we debug
        // TODO: Restore proper authorization after debugging
        boolean isRequester = request.getRequester().getId().equals(user.getId());
        boolean isAcceptor = request.getAcceptor() != null && request.getAcceptor().getId().equals(user.getId());
        boolean isResponder = neighborMessageService.hasRespondedToRequest(requestId, user.getId());
        
        // Log access information for debugging
        System.out.println("Chat access - User ID: " + user.getId() + 
                          ", Is Requester: " + isRequester + 
                          ", Is Acceptor: " + isAcceptor + 
                          ", Is Responder: " + isResponder);
        
        // Skip authorization check for now
        // if (!isRequester && !isAcceptor && !isResponder) {
        //     return ResponseEntity.status(403).body("Not authorized to view these responses");
        // }
        
        System.out.println("Getting messages for request ID: " + requestId + ", User ID: " + user.getId() + 
            ", IsRequester: " + isRequester + ", IsResponder: " + isResponder);
        
        // Get all messages for this request
        List<NeighborMessage> messages = neighborMessageService.getMessagesByRequest(requestId);
        
        // If user is a responder, only show messages between them and the requester
        if (isResponder) {
            messages = messages.stream()
                .filter(msg -> msg.getSender().getId().equals(user.getId()) || 
                             msg.getReceiver().getId().equals(user.getId()) ||
                             msg.getSender().getId().equals(request.getRequester().getId()) ||
                             msg.getReceiver().getId().equals(request.getRequester().getId()))
                .collect(Collectors.toList());
        }
        
        System.out.println("Found " + messages.size() + " messages for request ID: " + requestId);
        messages.forEach(msg -> {
            System.out.println("Message: ID=" + msg.getId() + ", Sender ID=" + msg.getSender().getId() + ", Receiver ID=" + msg.getReceiver().getId());
        });
        
        // Create a map to hold all messages (including requester's messages)
        Map<Long, List<NeighborMessage>> allMessages = new HashMap<>();
        
        // Group messages by sender
        messages.forEach(message -> {
            Long senderId = message.getSender().getId();
            allMessages.computeIfAbsent(senderId, k -> new ArrayList<>()).add(message);
            System.out.println("Added message to map for sender ID: " + senderId);
        });
        
        // Mark messages as read for the current user
        neighborMessageService.markMessagesAsRead(requestId, user.getId());
        
        System.out.println("Returning messages map with " + allMessages.size() + " senders");
        
        // Return all messages including the ones from the requester
        return ResponseEntity.ok(allMessages);
    }
    
    // Send a message response to a request
    @PostMapping("/message/send")
    @ResponseBody
    public ResponseEntity<?> sendMessage(
            @PathVariable Long communityId,
            @RequestParam("requestId") Long requestId,
            @RequestParam("message") String messageText,
            @RequestParam("receiverId") Long receiverId,
            HttpSession session) {
        
        System.out.println("Received send message request - Request ID: " + requestId + ", Receiver ID: " + receiverId);
        System.out.println("Message length: " + (messageText != null ? messageText.length() : 0) + " characters");
        
        User sender = (User) session.getAttribute("user");
        if (sender == null) {
            System.out.println("User not authenticated");
            return ResponseEntity.status(401).body("User not authenticated");
        }
        
        System.out.println("Sender ID: " + sender.getId() + ", Sender name: " + sender.getFullName());
        
        NeighborRequest request = neighborRequestService.getRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        User receiver = userService.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));
        
        // Don't allow sending messages to yourself
        if (sender.getId().equals(receiver.getId())) {
            return ResponseEntity.badRequest().body("You cannot send messages to yourself");
        }
        
        NeighborMessage message = new NeighborMessage();
        message.setRequest(request);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setMessage(messageText);
        message.setSentAt(LocalDateTime.now());
        message.setRead(false);
        
        NeighborMessage savedMessage = neighborMessageService.saveMessage(message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("messageId", savedMessage.getId());
        response.put("timestamp", savedMessage.getSentAt().toString());
        
        return ResponseEntity.ok(response);
    }
    
    // Delete a request (for requester)
    @PostMapping("/request/{requestId}/delete")
    public String deleteRequest(
            @PathVariable Long communityId,
            @PathVariable Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        Optional<NeighborRequest> requestOpt = neighborRequestService.getRequestById(requestId);
        
        if (requestOpt.isPresent()) {
            NeighborRequest request = requestOpt.get();
            
            // Check if user is the requester
            if (!request.getRequester().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to delete this request");
                return "redirect:/user/community/" + communityId + "/neighbor/my-requests";
            }
            
            neighborRequestService.deleteRequest(requestId);
            redirectAttributes.addFlashAttribute("successMessage", "Your request has been canceled and is no longer visible to neighbors");
        }
        
        return "redirect:/user/community/" + communityId + "/neighbor/my-requests";
    }
    
    // Mark request as resolved (for requester)
    @PostMapping("/request/{requestId}/resolve")
    public String resolveRequest(
            @PathVariable Long communityId,
            @PathVariable Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        Optional<NeighborRequest> requestOpt = neighborRequestService.getRequestById(requestId);
        
        if (requestOpt.isPresent()) {
            NeighborRequest request = requestOpt.get();
            
            // Check if user is the requester
            if (!request.getRequester().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to resolve this request");
                return "redirect:/user/community/" + communityId + "/neighbor/my-requests";
            }
            
            neighborRequestService.markRequestAsResolved(requestId);
            redirectAttributes.addFlashAttribute("successMessage", "Your request has been marked as resolved");
        }
        
        return "redirect:/user/community/" + communityId + "/neighbor/my-requests";
    }
    
    // Accept a request (for responder)
    @PostMapping("/request/{requestId}/accept")
    public String acceptRequest(
            @PathVariable Long communityId,
            @PathVariable Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        Optional<NeighborRequest> requestOpt = neighborRequestService.getRequestById(requestId);
        
        if (requestOpt.isPresent()) {
            NeighborRequest request = requestOpt.get();
            
            // Check if user is NOT the requester (only non-requesters can accept)
            if (request.getRequester().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You cannot accept your own request");
                return "redirect:/user/community/" + communityId + "/neighbor/help-requests";
            }
            
            // Set the acceptor and accepted timestamp
            request.setAcceptor(user);
            request.setAcceptedAt(LocalDateTime.now());
            neighborRequestService.saveRequest(request);
            
            // Create an initial message to notify the requester
            NeighborMessage message = new NeighborMessage();
            message.setRequest(request);
            message.setSender(user);
            message.setReceiver(request.getRequester());
            message.setMessage("I've accepted your request and I'm ready to help!");
            message.setSentAt(LocalDateTime.now());
            message.setRead(false);
            neighborMessageService.saveMessage(message);
            
            redirectAttributes.addFlashAttribute("successMessage", "You have accepted this request. A message has been sent to the requester.");
        }
        
        return "redirect:/user/community/" + communityId + "/neighbor/help-requests";
    }
}
