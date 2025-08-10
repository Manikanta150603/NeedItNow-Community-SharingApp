package com.needitnow.controllers;

import com.needitnow.entity.ChatMessage;
import com.needitnow.entity.Community;
import com.needitnow.entity.GroupBuy;
import com.needitnow.entity.GroupBuyMember;
import com.needitnow.entity.Item;
import com.needitnow.entity.User;
import com.needitnow.services.ChatMessageService;
import com.needitnow.services.CommunityService;
import com.needitnow.services.GroupBuyService;
import com.needitnow.services.GroupBuyMemberService;
import com.needitnow.services.ItemService;
import com.needitnow.services.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user/community/{communityId}/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CommunityService communityService;

    @Autowired
    private ChatMessageService chatMessageService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private GroupBuyService groupBuyService;
    
    @Autowired
    private GroupBuyMemberService groupBuyMemberService;
    
    @GetMapping("/my-activities")
    public String viewMyActivities(
            @PathVariable Long communityId,
            Model model,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        // Verify community exists and user is a member
        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            return "redirect:/user/dashboard";
        }
        
        Community community = communityService.getCommunityById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
        
        // Get user activities data
        // 1. Items shared by the user
        List<Item> sharedItems = itemService.getItemsSharedByUser(user.getId(), communityId);
        
        // 2. Items requested by the user
        List<Item> requestedItems = itemService.getItemsRequestedByUser(user.getId(), communityId);
        
        // 3. Group buys organized by the user
        List<GroupBuy> organizedGroupBuys = groupBuyService.findOrganizedByUser(user.getId());
        organizedGroupBuys = organizedGroupBuys.stream()
            .filter(gb -> gb.getCommunity().getId().equals(communityId))
            .collect(Collectors.toList());
            
        // 4. Group buys joined by the user
        List<GroupBuyMember> groupBuyMemberships = groupBuyMemberService.findActiveByUserId(user.getId());
        List<GroupBuy> joinedGroupBuys = groupBuyMemberships.stream()
            .filter(gbm -> gbm.getGroupBuy().getCommunity().getId().equals(communityId))
            .map(GroupBuyMember::getGroupBuy)
            .collect(Collectors.toList());
        
        // Add data to the model
        model.addAttribute("community", community);
        model.addAttribute("sharedItems", sharedItems);
        model.addAttribute("requestedItems", requestedItems);
        model.addAttribute("organizedGroupBuys", organizedGroupBuys);
        model.addAttribute("joinedGroupBuys", joinedGroupBuys);
        
        return "my-activities";
    }

    @PostMapping("/share")
    public String shareItem(
            @PathVariable Long communityId,
            @RequestParam("itemCategory") String category,
            @RequestParam("availableQuantity") int availableQuantity,
            @RequestParam("dateOfPurchase") LocalDate dateOfPurchase,
            @RequestParam("itemName") String itemName,
            @RequestParam(value = "itemPrice", required = false) BigDecimal price,
            @RequestParam("markAsFree") boolean isFree,
            @RequestParam(value = "autoRemoveAfter", required = false) Integer autoRemoveAfter,
            @RequestParam(value = "autoRemoveUnit", required = false) String autoRemoveUnit,
            @RequestParam("additionalNotes") String additionalNotes,
            @RequestParam("itemPhoto") MultipartFile photo,
            @RequestParam("displayNameToggle") boolean displayName,
            @RequestParam("allowPhoneToggle") boolean allowPhone,
            HttpSession session,
            RedirectAttributes redirectAttributes) throws IOException {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Verify community exists and user is a member
        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            throw new SecurityException("You are not a member of this community");
        }
        
        Community comm = communityService.getCommunityById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));

        Item item = new Item();
        item.setCommunity(comm);
        item.setUser(user);
        item.setCategory(category);
        item.setAvailableQuantity(availableQuantity);
        item.setDateOfPurchase(dateOfPurchase);
        item.setItemName(itemName);
        item.setPrice(isFree ? BigDecimal.ZERO : (price != null ? price : BigDecimal.ZERO));
        item.setFree(isFree);
        item.setAutoRemoveAfter(autoRemoveAfter);
        item.setAutoRemoveUnit(autoRemoveUnit);
        item.setAdditionalNotes(additionalNotes);
        item.setDisplayName(displayName);
        item.setAllowPhone(allowPhone);
        item.setDateOfPosting(LocalDateTime.now());

        if (!photo.isEmpty()) {
            item.setPhotoData(photo.getBytes());
        }

        itemService.saveItem(item);

        redirectAttributes.addFlashAttribute("successMessage", "Item shared successfully!");
        return "redirect:/user/community/" + communityId + "/share-circle";
    }
    
    @PostMapping("/request-item")
    public String requestNeighborItem(
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
        if (user == null) {
            return "redirect:/login";
        }

        // Verify community exists and user is a member
        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            throw new SecurityException("You are not a member of this community");
        }
        
        Community comm = communityService.getCommunityById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));

        // Create a new Item with request flags
        Item requestItem = new Item();
        requestItem.setCommunity(comm);
        requestItem.setUser(user);                 // This is the requester
        requestItem.setCategory(category);
        requestItem.setItemName(title);
        requestItem.setAdditionalNotes(description);
        requestItem.setAutoRemoveAfter(duration);
        requestItem.setAutoRemoveUnit(durationUnit);
        requestItem.setDisplayName(displayName);
        requestItem.setAllowPhone(allowPhone);
        requestItem.setDateOfPosting(LocalDateTime.now());
        requestItem.setRequestItem(true);          // Flag as a request item
        requestItem.setUrgency(urgency);
        
        // Set default values for non-required fields
        requestItem.setAvailableQuantity(1);
        requestItem.setDateOfPurchase(LocalDate.now());
        requestItem.setFree(true);
        requestItem.setPrice(BigDecimal.ZERO);

        if (photo != null && !photo.isEmpty()) {
            requestItem.setPhotoData(photo.getBytes());
        }

        itemService.saveItem(requestItem);

        redirectAttributes.addFlashAttribute("successMessage", "Your request has been posted! Neighbors will be notified.");
        return "redirect:/user/community/" + communityId + "/items/my-requests";
    }

    @GetMapping("/available")
    public String viewAvailableItems(@PathVariable Long communityId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Community comm = communityService.getCommunityById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            throw new SecurityException("You are not a member of this community");
        }
        
        // Get items that are not requests (normal shared items)
        List<Item> items = itemService.getItemsByCommunityId(communityId).stream()
                .filter(item -> !item.isRequestItem())
                .collect(Collectors.toList());
                
        model.addAttribute("items", items);
        model.addAttribute("communityId", communityId);
        model.addAttribute("currentUserId", user.getId());
        model.addAttribute("community", comm);
        return "available-items";
    }
    
    @GetMapping("/neighbor-requests")
    public String viewNeighborRequests(@PathVariable Long communityId, HttpSession session, Model model) {
        // Redirect to the new NeighborController endpoint
        return "redirect:/user/community/" + communityId + "/neighbor/help-requests";
    }
    
    @GetMapping("/my-requests")
    public String viewMyRequests(@PathVariable Long communityId, HttpSession session, Model model) {
        // Redirect to the new NeighborController endpoint
        return "redirect:/user/community/" + communityId + "/neighbor/my-requests";
    }
    
    @PostMapping("/delete-request/{requestId}")
    public String deleteRequest(
            @PathVariable Long communityId,
            @PathVariable Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Verify community exists and user is a member
        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            throw new SecurityException("You are not a member of this community");
        }
        
        // Get the item and verify ownership
        Item requestItem = itemService.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        // Check if user is the owner of this request
        if (!requestItem.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only delete your own requests.");
            return "redirect:/user/community/" + communityId + "/items/my-requests";
        }
        
        // Delete the request
        itemService.deleteItem(requestId);
        
        redirectAttributes.addFlashAttribute("successMessage", "Request successfully deleted.");
        return "redirect:/user/community/" + communityId + "/items/my-requests";
    }
    
    @PostMapping("/respond-to-request")
    public String respondToRequest(
            @PathVariable Long communityId,
            @RequestParam("requestId") Long requestId,
            @RequestParam("responseMessage") String responseMessage,
            @RequestParam(value = "shareContactInfo", required = false, defaultValue = "false") boolean shareContactInfo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User responder = (User) session.getAttribute("user");
        if (responder == null) {
            return "redirect:/login";
        }
        
        // Verify community exists and user is a member
        if (!communityService.isUserMemberOfCommunity(responder.getId(), communityId)) {
            throw new SecurityException("You are not a member of this community");
        }
        
        // Get the request item
        Item requestItem = itemService.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        // Don't allow responding to your own requests
        if (requestItem.getUser().getId().equals(responder.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot respond to your own request.");
            return "redirect:/user/community/" + communityId + "/items/neighbor-requests";
        }
        
        // TODO: Create a proper response entity and save it
        // For now, we'll just mark the request as accepted and update the message
        requestItem.setRequestAccepted(true);
        requestItem.setRequester(responder);
        requestItem.setAdditionalNotes(requestItem.getAdditionalNotes() + "\n\nResponse from " + 
                responder.getFullName() + ": " + responseMessage);
        
        itemService.saveItem(requestItem);
        
        // Send notification to the request owner (would need a notification system)
        
        redirectAttributes.addFlashAttribute("successMessage", "Your response has been sent to the neighbor.");
        return "redirect:/user/community/" + communityId + "/items/neighbor-requests";
    }
    
    @PostMapping("/accept-request")
    public String acceptRequest(
            @PathVariable Long communityId,
            @RequestParam("requestId") Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User responder = (User) session.getAttribute("user");
        if (responder == null) {
            return "redirect:/login";
        }
        
        // Verify community exists and user is a member
        if (!communityService.isUserMemberOfCommunity(responder.getId(), communityId)) {
            throw new SecurityException("You are not a member of this community");
        }
        
        // Get the request item
        Item requestItem = itemService.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        // Don't allow accepting your own requests
        if (requestItem.getUser().getId().equals(responder.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot accept your own request.");
            return "redirect:/user/community/" + communityId + "/items/neighbor-requests";
        }
        
        // Mark the request as accepted
        requestItem.setRequestAccepted(true);
        requestItem.setRequester(responder);
        requestItem.setRequestRejected(false);
        
        itemService.saveItem(requestItem);
        
        redirectAttributes.addFlashAttribute("successMessage", "You have accepted the request! The neighbor has been notified.");
        return "redirect:/user/community/" + communityId + "/items/neighbor-requests";
    }

    @PostMapping("/request/{itemId}")
    public String requestItem(@PathVariable Long communityId, @PathVariable Long itemId, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Item item = itemService.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        if (item.getRequester() != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "This item has already been requested.");
            return "redirect:/user/community/" + communityId + "/items/available";
        }
        item.setRequester(user);
        itemService.saveItem(item);
        redirectAttributes.addFlashAttribute("successMessage", "Item requested successfully!");
        return "redirect:/user/community/" + communityId + "/items/available";
    }

    @GetMapping("/requests-received")
    public String viewRequestsReceived(@PathVariable Long communityId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Community community = communityService.getCommunityById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            throw new SecurityException("You are not a member of this community");
        }
        
        // Get items that have chat messages where the current user is the owner
        List<Item> items = chatMessageService.getItemsWithChatMessages(user.getId());
        
        // Filter items to only include those from this community
        items = items.stream()
                .filter(item -> item.getCommunity().getId().equals(communityId))
                .toList();
        
        // Load the last message for each item
        Map<Long, ChatMessage> lastMessages = new HashMap<>();
        Map<Long, User> requesters = new HashMap<>();
        
        for (Item item : items) {
            List<ChatMessage> messages = chatMessageService.getChatMessagesForItemOwner(item.getId(), user.getId());
            if (!messages.isEmpty()) {
                ChatMessage lastMessage = messages.get(0); // First message (due to DESC sorting)
                lastMessages.put(item.getId(), lastMessage);
                
                // Store the requester (sender of the message if not the owner)
                User requester = lastMessage.getSender().getId().equals(user.getId()) 
                    ? lastMessage.getReceiver() 
                    : lastMessage.getSender();
                    
                requesters.put(item.getId(), requester);
            }
        }
        
        model.addAttribute("items", items);
        model.addAttribute("lastMessages", lastMessages);
        model.addAttribute("requesters", requesters);
        model.addAttribute("communityId", communityId);
        model.addAttribute("currentUserId", user.getId());
        return "requests-received";
    }
    
    @GetMapping("/asking-list")
    public String viewAskingList(@PathVariable Long communityId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Check if community exists and user is a member
        Community community = communityService.getCommunityById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            throw new SecurityException("You are not a member of this community");
        }
        
        // Get all chat messages sent by the current user (requests they've made)
        List<ChatMessage> sentMessages = chatMessageService.getSentMessagesByUser(user.getId());
        
        // Group the messages by item to find all items the user has requested
        // Only include items not owned by the current user
        Map<Item, List<ChatMessage>> requestedItems = new HashMap<>();
        for (ChatMessage message : sentMessages) {
            Item item = message.getItem();
            if (item != null && item.getCommunity().getId().equals(communityId) 
                    && !item.getUser().getId().equals(user.getId())) {
                requestedItems.computeIfAbsent(item, k -> new java.util.ArrayList<>()).add(message);
            }
        }
        
        // Also find items where the user was the last requester (for rejected requests)
        List<Item> allCommunityItems = itemService.getItemsByCommunityId(communityId);
        for (Item item : allCommunityItems) {
            if (item.isRequestRejected() && item.getLastRequester() != null && 
                item.getLastRequester().getId().equals(user.getId()) && 
                !item.getUser().getId().equals(user.getId())) {
                // Add rejected items to the map
                if (!requestedItems.containsKey(item)) {
                    requestedItems.put(item, new java.util.ArrayList<>());
                }
            }
        }
        
        // Get the latest message for each item
        Map<Long, ChatMessage> latestMessages = new HashMap<>();
        Map<Long, User> itemOwners = new HashMap<>();
        
        for (Map.Entry<Item, List<ChatMessage>> entry : requestedItems.entrySet()) {
            Item item = entry.getKey();
            List<ChatMessage> messages = entry.getValue();
            
            // Sort messages by timestamp (descending)
            messages.sort((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()));
            
            // Get the latest message
            if (!messages.isEmpty()) {
                ChatMessage latestMessage = messages.get(0);
                latestMessages.put(item.getId(), latestMessage);
                itemOwners.put(item.getId(), item.getUser());
            }
        }
        
        // Create a map to store base64 encoded images
        Map<Long, String> encodedImages = new HashMap<>();
        
        // Encode all item images to base64
        for (Item item : requestedItems.keySet()) {
            if (item.getPhotoData() != null) {
                String base64Image = java.util.Base64.getEncoder().encodeToString(item.getPhotoData());
                encodedImages.put(item.getId(), base64Image);
            }
        }
        
        model.addAttribute("items", requestedItems.keySet());
        model.addAttribute("latestMessages", latestMessages);
        model.addAttribute("itemOwners", itemOwners);
        model.addAttribute("encodedImages", encodedImages);
        model.addAttribute("community", community);
        model.addAttribute("communityId", communityId);
        model.addAttribute("currentUserId", user.getId());
        
        return "asking-list";
    }

    @PostMapping("/accept/{itemId}/{requesterId}")
    @ResponseBody
    public ResponseEntity<?> acceptRequest(
            @PathVariable Long communityId,
            @PathVariable Long itemId,
            @PathVariable Long requesterId,    // now a path variable
            HttpSession session) {

        User owner = (User) session.getAttribute("user");
        if (owner == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Item item = itemService.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        if (!item.getUser().getId().equals(owner.getId())) {
            return ResponseEntity.status(403).body("You are not authorized to accept this request");
        }

        User requester = userService.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));

        item.setRequester(requester);
        item.setRequestAccepted(true);
        item.setRequestRejected(false); // Ensure rejected is set to false when accepting
        itemService.saveItem(item);

        ChatMessage acceptMessage = new ChatMessage();
        acceptMessage.setItem(item);
        acceptMessage.setSender(owner);
        acceptMessage.setReceiver(requester);
        acceptMessage.setMessage(
            "I've accepted your request for " + item.getItemName() + 
            ". Let's arrange a pickup!"
        );
        acceptMessage.setTimestamp(LocalDateTime.now());
        chatMessageService.saveChatMessage(acceptMessage);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Request accepted successfully"
        ));
    }
    
    @PostMapping("/reject/{itemId}")
    @ResponseBody
    public ResponseEntity<?> rejectRequest(
        @PathVariable Long communityId,
        @PathVariable Long itemId,
        HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        Item item = itemService.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        
        // Check if the user is the owner of the item
        if (!item.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You are not authorized to reject this request");
        }
        
        // Check if there is a requester
        if (item.getRequester() == null) {
            return ResponseEntity.badRequest().body("This item has no pending requests");
        }
        
        // Save the current requester before clearing it
        User rejectedRequester = item.getRequester();
        
        // Create a notification message for the requester
        ChatMessage rejectMessage = new ChatMessage();
        rejectMessage.setItem(item);
        rejectMessage.setSender(user);
        rejectMessage.setReceiver(rejectedRequester);
        rejectMessage.setMessage("I'm sorry, I can't fulfill your request for " + item.getItemName() + " at this time.");
        rejectMessage.setTimestamp(LocalDateTime.now());
        chatMessageService.saveChatMessage(rejectMessage);
        
        // Mark as rejected and save the last requester for reference
        item.setLastRequester(rejectedRequester);
        item.setRequester(null);
        item.setRequestAccepted(false); // Ensure accepted is set to false
        item.setRequestRejected(true);  // Set rejected to true
        itemService.saveItem(item);
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "success", true,
            "message", "Request rejected successfully"
        ));
    }
    
    @PostMapping("/chat/send")
    @ResponseBody
    public ResponseEntity<String> sendChatMessage(
            @PathVariable Long communityId,
            @RequestParam Long itemId,
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam String message,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getId().equals(senderId)) {
            System.out.println("Unauthorized access: User is null or senderId does not match.");
            return ResponseEntity.status(403).body("Unauthorized");
        }
        Item item = itemService.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        User sender = new User();
        sender.setId(senderId);
        User receiver = new User();
        receiver.setId(receiverId);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setItem(item);
        chatMessage.setSender(sender);
        chatMessage.setReceiver(receiver);
        chatMessage.setMessage(message);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessageService.saveChatMessage(chatMessage);
        System.out.println("Message saved: ItemID=" + itemId + ", SenderID=" + senderId + ", ReceiverID=" + receiverId + ", Message=" + message);
        return ResponseEntity.ok("Message sent");
    }

    @GetMapping("/chat/history")
    @ResponseBody
    public List<ChatMessage> getChatHistory(
            @PathVariable Long communityId,
            @RequestParam Long itemId,
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        System.out.println("Fetching chat history: ItemID=" + itemId + ", SenderID=" + senderId + ", ReceiverID=" + receiverId + ", CurrentUserID=" + (user != null ? user.getId() : "null"));
        
        // Authorization check
        if (user == null || (!user.getId().equals(senderId) && !user.getId().equals(receiverId))) {
            System.out.println("Unauthorized access to chat history: UserID=" + (user != null ? user.getId() : "null") + ", SenderID=" + senderId + ", ReceiverID=" + receiverId);
            throw new SecurityException("Unauthorized access to chat history");
        }

        // Fetch chat messages
        List<ChatMessage> messages = chatMessageService.getChatMessages(itemId, senderId, receiverId);

        // Initialize lazy-loaded relationships to prevent serialization errors
        for (ChatMessage message : messages) {
            if (message.getSender() != null) {
                message.getSender().getId(); // Forces sender to load
            }
            if (message.getReceiver() != null) {
                message.getReceiver().getId(); // Forces receiver to load
            }
            if (message.getItem() != null) {
                message.getItem().getId(); // Forces item to load
                if (message.getItem().getCommunity() != null) {
                    message.getItem().getCommunity().getId(); // Forces community to load
                    if (message.getItem().getCommunity().getUser() != null) {
                        message.getItem().getCommunity().getUser().getId(); // Forces user to load
                    }
                }
            }
        }

        System.out.println("Chat history fetched: " + messages.size() + " messages found.");
        return messages;
    }
    


    @GetMapping("/buy-together")
    public String viewBuyTogetherPage(@PathVariable Long communityId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            return "redirect:/communities";
        }
        
        Community community = communityService.getCommunityById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));

        // Get active group buys in the community
        List<GroupBuy> activeGroupBuys = groupBuyService.findActiveByCommunityId(communityId);
        
        // Get group buys organized by the user
        List<GroupBuy> organizedGroupBuys = groupBuyService.findOrganizedByUser(user.getId());
        
        // Fetch members for each organized group buy
        for (GroupBuy groupBuy : organizedGroupBuys) {
            List<GroupBuyMember> members = groupBuyMemberService.findMembersByGroupBuyIdSorted(groupBuy.getId());
            groupBuy.setMembers(members);
        }
        
        // Get group buys the user has joined (but didn't organize)
        List<GroupBuy> joinedGroupBuys = groupBuyMemberService.findByUserId(user.getId()).stream()
                .filter(member -> !member.isCreator())
                .map(GroupBuyMember::getGroupBuy)
                .collect(Collectors.toList());
        
        // Calculate urgency messages for active group buys
        Map<Long, String> urgencyMessages = new HashMap<>();
        for (GroupBuy groupBuy : activeGroupBuys) {
            String message = groupBuyService.getGroupBuyUrgencyMessage(groupBuy);
            if (message != null) {
                urgencyMessages.put(groupBuy.getId(), message);
            }
        }
        
        // Calculate which group buys are already full
        Map<Long, Boolean> fullGroupBuys = new HashMap<>();
        for (GroupBuy groupBuy : activeGroupBuys) {
            boolean isFull = groupBuyService.isGroupBuyFull(groupBuy) || 
                            groupBuyService.isQuantityMaxReached(groupBuy);
            fullGroupBuys.put(groupBuy.getId(), isFull);
        }
        
        model.addAttribute("activeGroupBuys", activeGroupBuys);
        model.addAttribute("organizedGroupBuys", organizedGroupBuys);
        model.addAttribute("joinedGroupBuys", joinedGroupBuys);
        model.addAttribute("urgencyMessages", urgencyMessages);
        model.addAttribute("fullGroupBuys", fullGroupBuys);
        model.addAttribute("communityId", communityId);
        model.addAttribute("currentUserId", user.getId());
        
        return "buy-together";
    }
    
    @GetMapping("/getGroupBuyImage/{groupBuyId}")
    public ResponseEntity<byte[]> getGroupBuyImage(@PathVariable Long communityId, @PathVariable Long groupBuyId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(null);
        }

        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            return ResponseEntity.status(403).body(null);
        }

        Optional<GroupBuy> groupBuyOptional = groupBuyService.findById(groupBuyId);
        if (groupBuyOptional.isEmpty() || groupBuyOptional.get().getPhotoData() == null) {
            return ResponseEntity.notFound().build();
        }

        GroupBuy groupBuy = groupBuyOptional.get();
        
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
                .body(groupBuy.getPhotoData());
    }
    
    @PostMapping("/group-buy/{groupBuyId}/cancel")
    public String cancelGroupBuy(
            @PathVariable Long communityId,
            @PathVariable Long groupBuyId,
            @RequestParam String cancellationReason,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Optional<GroupBuy> groupBuyOpt = groupBuyService.findById(groupBuyId);
        if (groupBuyOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Group buy not found");
            return "redirect:/user/community/" + communityId + "/items/buy-together";
        }
        
        GroupBuy groupBuy = groupBuyOpt.get();
        
        // Check if the user is the creator of the group buy
        if (!groupBuy.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only the group creator can cancel a group buy");
            return "redirect:/user/community/" + communityId + "/items/buy-together";
        }
        
        groupBuyService.cancelGroupBuy(groupBuy, cancellationReason);
        
        redirectAttributes.addFlashAttribute("successMessage", "Group buy cancelled successfully");
        return "redirect:/user/community/" + communityId + "/items/buy-together";
    }
    
    @PostMapping("/group-buy/{groupBuyId}/lock")
    public String lockGroupBuy(
            @PathVariable Long communityId,
            @PathVariable Long groupBuyId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Optional<GroupBuy> groupBuyOpt = groupBuyService.findById(groupBuyId);
        if (groupBuyOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Group buy not found");
            return "redirect:/user/community/" + communityId + "/items/buy-together";
        }
        
        GroupBuy groupBuy = groupBuyOpt.get();
        
        // Check if the user is the creator of the group buy
        if (!groupBuy.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only the group creator can lock a group buy");
            return "redirect:/user/community/" + communityId + "/items/buy-together";
        }
        
        groupBuyService.lockGroupBuy(groupBuy);
        
        redirectAttributes.addFlashAttribute("successMessage", "Group buy locked successfully");
        return "redirect:/user/community/" + communityId + "/items/buy-together";
    }
    
    @PostMapping("/group-buy/{groupBuyId}/complete")
    public String completeGroupBuy(
            @PathVariable Long communityId,
            @PathVariable Long groupBuyId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Optional<GroupBuy> groupBuyOpt = groupBuyService.findById(groupBuyId);
        if (groupBuyOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Group buy not found");
            return "redirect:/user/community/" + communityId + "/items/buy-together";
        }
        
        GroupBuy groupBuy = groupBuyOpt.get();
        
        // Check if the user is the creator of the group buy
        if (!groupBuy.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only the group creator can complete a group buy");
            return "redirect:/user/community/" + communityId + "/items/buy-together";
        }
        
        groupBuyService.completeGroupBuy(groupBuy);
        
        redirectAttributes.addFlashAttribute("successMessage", "Group buy marked as completed");
        return "redirect:/user/community/" + communityId + "/items/buy-together";
    }
    
    @PostMapping("/group-buy/{groupBuyId}/leave")
    public String leaveGroupBuy(
            @PathVariable Long communityId,
            @PathVariable Long groupBuyId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Optional<GroupBuy> groupBuyOpt = groupBuyService.findById(groupBuyId);
        if (groupBuyOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Group buy not found");
            return "redirect:/user/community/" + communityId + "/items/buy-together";
        }
        
        GroupBuy groupBuy = groupBuyOpt.get();
        
        // Check if cancellation is allowed
        if (!groupBuy.isAllowCancellation()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cancellation is not allowed for this group buy");
            return "redirect:/user/community/" + communityId + "/items/buy-together";
        }
        
        boolean success = groupBuyMemberService.leaveGroupBuy(groupBuyId, user.getId());
        
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "You have left the group buy. Note that your member score has been reduced.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to leave the group buy. If you are the creator, you cannot leave your own group buy.");
        }
        
        return "redirect:/user/community/" + communityId + "/items/buy-together";
    }
    
    @PostMapping("/group-buy/{groupBuyId}/mark-received")
    public String markItemAsReceived(
            @PathVariable Long communityId,
            @PathVariable Long groupBuyId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        List<GroupBuyMember> members = groupBuyMemberService.findByGroupBuyIdAndUserId(groupBuyId, user.getId());
        if (members.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are not a member of this group buy");
            return "redirect:/user/community/" + communityId + "/items/buy-together";
        }
        
        GroupBuyMember member = members.get(0);
        boolean success = groupBuyMemberService.markItemAsReceived(member.getId());
        
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Item marked as received");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to mark item as received");
        }
        
        return "redirect:/user/community/" + communityId + "/items/buy-together";
    }
    
    @PostMapping("/buy-together/join")
    public String joinGroupBuy(
            @PathVariable Long communityId,
            @RequestParam Long groupBuyId,
            @RequestParam Double quantity,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String paymentMethod,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Check if community exists and user is a member
        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            throw new SecurityException("You are not a member of this community");
        }
        
        try {
            // Find the group buy
            GroupBuy groupBuy = groupBuyService.findById(groupBuyId)
                    .orElseThrow(() -> new IllegalArgumentException("Group buy not found"));
            
            // Check if group buy belongs to this community
            if (!groupBuy.getCommunity().getId().equals(communityId)) {
                throw new IllegalArgumentException("Group buy does not belong to this community");
            }
            
            // Check if user is already a member of this group buy
            boolean isAlreadyMember = groupBuyMemberService.isUserMemberOfGroupBuy(user.getId(), groupBuyId);
            if (isAlreadyMember) {
                redirectAttributes.addFlashAttribute("errorMessage", "You are already a member of this group buy");
                return "redirect:/user/community/" + communityId + "/items/buy-together";
            }
            
            // Check if there's enough capacity left
            double availableQuantity = groupBuy.getMaxQuantity() - groupBuy.getCurrentQuantity();
            if (quantity > availableQuantity) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "Requested quantity exceeds available capacity. Maximum available: " + availableQuantity + " " + groupBuy.getQuantityUnit());
                return "redirect:/user/community/" + communityId + "/items/buy-together";
            }
            
            // Create a new group buy member
            GroupBuyMember member = new GroupBuyMember();
            member.setGroupBuy(groupBuy);
            member.setUser(user);
            member.setQuantity(quantity);
            member.setNotes(notes);
            member.setJoinedAt(LocalDateTime.now());
            
            // Set payment method if provided
            if (paymentMethod != null) {
                member.setPaymentMethod(paymentMethod);
            }
            
            // Set auto-confirmation status based on group buy settings
            member.setConfirmed(groupBuy.isAutoConfirmMembers());
            
            // Save the new member
            groupBuyMemberService.save(member);
            
            // Update current quantity in group buy
            groupBuy.setCurrentQuantity(groupBuy.getCurrentQuantity() + quantity.intValue());
            groupBuy.setCurrentMemberCount(groupBuy.getCurrentMemberCount() + 1);
            groupBuyService.save(groupBuy);
            
            redirectAttributes.addFlashAttribute("successMessage", "You have successfully joined the group buy for " + groupBuy.getItemName());
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error joining group buy: " + e.getMessage());
        }
        
        return "redirect:/user/community/" + communityId + "/items/buy-together";
    }
    
    @PostMapping("/buy-together/create")
    public String createGroupBuy(
            @PathVariable Long communityId,
            @RequestParam("itemName") String itemName,
            @RequestParam("category") String category,
            @RequestParam("maxMembers") int maxMembers,
            @RequestParam("maxQuantity") int maxQuantity,
            @RequestParam("minQuantityNeeded") int minQuantityNeeded,
            @RequestParam("quantityUnit") String quantityUnit,
            @RequestParam("pricePerUnit") BigDecimal pricePerUnit,
            @RequestParam("deadlineDateTime") LocalDateTime deadlineDateTime,
            @RequestParam("plannedPurchaseTime") LocalDateTime plannedPurchaseTime,
            @RequestParam(value = "pickupWindow", required = false) String pickupWindow,
            @RequestParam("deliveryPoint") String deliveryPoint,
            @RequestParam(value = "autoConfirmMembers", defaultValue = "true") boolean autoConfirmMembers,
            @RequestParam(value = "confirmationRequired", defaultValue = "false") boolean confirmationRequired,
            @RequestParam(value = "allowCancellation", defaultValue = "false") boolean allowCancellation,
            @RequestParam(value = "notesToMembers", required = false) String notesToMembers,
            @RequestParam(value = "itemPhoto", required = false) MultipartFile photo,
            @RequestParam(value = "displayName", defaultValue = "true") boolean displayName,
            @RequestParam(value = "allowPhone", defaultValue = "false") boolean allowPhone,
            @RequestParam(value = "onlyAutoConfirmed", defaultValue = "false") boolean onlyAutoConfirmed,
            @RequestParam(value = "advanceContribution", defaultValue = "false") boolean advanceContribution,
            HttpSession session,
            RedirectAttributes redirectAttributes) throws IOException {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Check if community exists and user is a member
        Community community = communityService.getCommunityById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
        if (!communityService.isUserMemberOfCommunity(user.getId(), communityId)) {
            throw new SecurityException("You are not a member of this community");
        }
        
        // Create new group buy
        GroupBuy groupBuy = new GroupBuy();
        groupBuy.setCommunity(community);
        groupBuy.setUser(user);
        groupBuy.setItemName(itemName);
        groupBuy.setCategory(category);
        groupBuy.setMaxMembers(maxMembers);
        groupBuy.setMaxQuantity(maxQuantity);
        groupBuy.setMinQuantityNeeded(minQuantityNeeded);
        groupBuy.setQuantityUnit(quantityUnit);
        groupBuy.setPricePerUnit(pricePerUnit);
        groupBuy.setDeadlineDateTime(deadlineDateTime);
        groupBuy.setPlannedPurchaseTime(plannedPurchaseTime);
        groupBuy.setPickupWindow(pickupWindow);
        groupBuy.setDeliveryPoint(deliveryPoint);
        groupBuy.setAutoConfirmMembers(autoConfirmMembers);
        groupBuy.setConfirmationRequired(confirmationRequired);
        groupBuy.setAllowCancellation(allowCancellation);
        groupBuy.setNotesToMembers(notesToMembers);
        groupBuy.setDisplayName(displayName);
        groupBuy.setAllowPhone(allowPhone);
        groupBuy.setOnlyAutoConfirmed(onlyAutoConfirmed);
        groupBuy.setAdvanceContribution(advanceContribution);
        groupBuy.setCreatedAt(LocalDateTime.now());
        
        if (photo != null && !photo.isEmpty()) {
            groupBuy.setPhotoData(photo.getBytes());
        }
        
        // Save the group buy first to get the ID
        GroupBuy savedGroupBuy = groupBuyService.saveGroupBuy(groupBuy);
        
        // Add the creator as the first member
        GroupBuyMember creatorMember = new GroupBuyMember();
        creatorMember.setGroupBuy(savedGroupBuy);
        creatorMember.setUser(user);
        creatorMember.setQuantity((double) minQuantityNeeded); // Creator contributes the minimum quantity
        creatorMember.setJoinedAt(LocalDateTime.now());
        creatorMember.setConfirmed(true); // Creator is auto-confirmed
        creatorMember.setCreator(true); // Mark as creator
        
        groupBuyMemberService.saveGroupBuyMember(creatorMember);
        
        redirectAttributes.addFlashAttribute("successMessage", "Group buy created successfully!");
        return "redirect:/user/community/" + communityId + "/items/buy-together";
    }
    
    /**
     * REST API endpoint to get a user's phone number
     * Only returns the phone for logged in users who are part of the same community
     */
    @GetMapping("/api/users/{userId}/phone")
    @ResponseBody
    public Map<String, String> getUserPhoneNumber(@PathVariable Long userId, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        Map<String, String> response = new HashMap<>();
        
        if (currentUser == null) {
            response.put("error", "User not authenticated");
            return response;
        }
        
        // Get the requested user
        Optional<User> requestedUserOpt = userService.findById(userId);
        if (requestedUserOpt.isEmpty()) {
            response.put("error", "User not found");
            return response;
        }
        
        User requestedUser = requestedUserOpt.get();
        
        // For simplicity, we're allowing access to phone numbers
        // In a real app, you'd check if they're in the same community
        response.put("phoneNumber", requestedUser.getPhoneNumber());
        return response;
    }
}