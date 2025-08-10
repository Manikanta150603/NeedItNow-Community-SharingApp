package com.needitnow.controllers;

import com.needitnow.entity.Community;
import com.needitnow.entity.CommunityJoinRequest;
import com.needitnow.entity.User;
import com.needitnow.entity.Item;
import com.needitnow.entity.GroupBuy;
import com.needitnow.entity.GroupBuyMember;
import com.needitnow.repositories.CommunityRepository;
import com.needitnow.services.CommunityService;
import com.needitnow.services.ItemService;
import com.needitnow.services.GroupBuyService;
import com.needitnow.services.GroupBuyMemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user/community")
public class CommunityController {
    
    @Autowired
    private CommunityService communityService;

    @Autowired
    private ItemService itemService;
    
    @Autowired
    private GroupBuyService groupBuyService;
    
    @Autowired
    private GroupBuyMemberService groupBuyMemberService;

    @Autowired
    private CommunityRepository communityRepository;

    // This method will be called before any request handler method
    @ModelAttribute
    public void addUserToModel(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
        }
    }

    @GetMapping("/{id}")
    public String viewCommunity(
            @PathVariable("id") Long communityId,
            Model model,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        try {
            // Get community details
            Community community = communityService.getCommunityById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
                
            // Check if user is a member or admin of this community
            boolean isMember = communityService.isUserMemberOfCommunity(user.getId(), communityId);
            boolean isAdmin = community.getUser().getId().equals(user.getId());
            
            if (!isMember && !isAdmin) {
                return "redirect:/user/community/" + communityId + "/join";
            }
            
            // Get member count
            long memberCount = communityService.getMemberCount(communityId);
            
            // Add attributes to model
            model.addAttribute("community", community);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("isMember", isMember);
            model.addAttribute("memberCount", memberCount);
            model.addAttribute("eventCount", 0); // Placeholder - implement as needed
            model.addAttribute("announcementCount", 0); // Placeholder - implement as needed
            
            return "community-dashboard";
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/{id}/share-circle")
    public String viewShareCircle(
            @PathVariable("id") Long communityId,
            Model model,
            HttpSession session) {
            
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        try {
            // Get community details
            Community community = communityService.getCommunityById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
                
            // Check if user is a member or admin of this community
            boolean isMember = communityService.isUserMemberOfCommunity(user.getId(), communityId);
            boolean isAdmin = community.getUser().getId().equals(user.getId());
            
            if (!isMember && !isAdmin) {
                return "redirect:/user/community/" + communityId + "/join";
            }
            
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
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("isMember", isMember);
            model.addAttribute("sharedItems", sharedItems);
            model.addAttribute("requestedItems", requestedItems);
            model.addAttribute("organizedGroupBuys", organizedGroupBuys);
            model.addAttribute("joinedGroupBuys", joinedGroupBuys);
            
            return "share-circle";
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/api/nearby-communities")
    @ResponseBody
    public ResponseEntity<?> getNearbyCommunities(
            @RequestParam("lat") double latitude,
            @RequestParam("lng") double longitude,
            @RequestParam(value = "radius", defaultValue = "5.0") double radius) {
        try {
            System.out.println("Searching for communities near: (" + latitude + ", " + longitude + ") with radius " + radius + "km");
            
            // Log input parameters
            System.out.println(String.format("Input - Latitude: %f, Longitude: %f, Radius: %f km", 
                latitude, longitude, radius));
            
            List<Community> communities = communityService.findNearbyCommunities(latitude, longitude, radius);
            
            // Log the number of communities found
            System.out.println("Found " + communities.size() + " communities");
            if (!communities.isEmpty()) {
                System.out.println("Sample community: " + communities.get(0).getCommunityName() + 
                    " at (" + communities.get(0).getLatitude() + ", " + communities.get(0).getLongitude() + ")");
            }
            
            return ResponseEntity.ok(communities);
        } catch (Exception e) {
            System.err.println("Error in getNearbyCommunities: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error finding nearby communities: " + e.getMessage());
        }
    }
    
    @GetMapping("/my-communities")
    public String showMyCommunitiesPage(
            @RequestParam(name = "tab", defaultValue = "my-communities") String activeTab,
            Model model, 
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        List<Community> userCommunities = communityService.getCommunitiesWhereUserIsMember(user.getId());
        model.addAttribute("userCommunities", userCommunities);
        
        if ("manage-requests".equals(activeTab)) {
            List<CommunityJoinRequest> joinRequests = communityService.getPendingRequestsForAdmin(user.getId());
            model.addAttribute("joinRequests", joinRequests);
        }
        
        if ("pending-requests".equals(activeTab)) {
            List<CommunityJoinRequest> pendingRequests = communityService.getPendingRequestsForUser(user);
            model.addAttribute("pendingRequests", pendingRequests);
        }
        
        int pendingCount = communityService.getPendingRequestCount(user.getId());
        model.addAttribute("pendingRequestCount", pendingCount);
        
        int userPendingCount = communityService.getUserPendingRequestCount(user);
        model.addAttribute("userPendingRequestCount", userPendingCount);
        
        model.addAttribute("activeTab", activeTab);
        
        return "my-communities";
    }
    
    @PostMapping("/cancel-request")
    public String cancelRequest(
            @RequestParam("requestId") Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        try {
            String result = communityService.cancelRequest(requestId, user.getId());
            redirectAttributes.addFlashAttribute("pendingMsg", result);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("pendingMsg", "Error: " + e.getMessage());
        }
        
        return "redirect:/user/community/my-communities?tab=pending-requests";
    }
    
    
    @GetMapping("/join")
    public String showJoinCommunityPage(
            @RequestParam(value = "search", required = false) String searchQuery,
            Model model) {
        
        try {
            List<Community> communities = communityService.searchCommunities(searchQuery);
            model.addAttribute("communities", communities);
            
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                model.addAttribute("searchQuery", searchQuery);
                
                if (communities.isEmpty()) {
                    model.addAttribute("infoMessage", 
                        "No communities found matching your search. Try different keywords or create a new community.");
                }
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", 
                "An error occurred while searching for communities. Please try again later.");
            System.err.println("Error searching communities: " + e.getMessage());
        }
        
        return "join-community";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }
        model.addAttribute("community", new Community());
        return "fragments/community-form :: communityForm";
    }

    @PostMapping("/create")
public String createCommunity(
        @RequestParam("name") String communityName,
        @RequestParam("address") String address,
        @RequestParam("city") String city,
        @RequestParam("state") String state,
        @RequestParam("pincode") String pincode,
        @RequestParam("country") String country,
        @RequestParam(value = "latitude", required = false) Double latitude,
        @RequestParam(value = "longitude", required = false) Double longitude,
        HttpSession session,
        RedirectAttributes redirectAttributes) {

    User user = (User) session.getAttribute("user");
    if (user == null) {
        return "redirect:/login";
    }

    try {
        Community community = new Community(null, communityName, address, city, 
            state, pincode, country, latitude, longitude, user);
        communityService.saveCommunity(community);
        redirectAttributes.addFlashAttribute("successMessage", "Community created successfully!");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage", "Error creating community: " + e.getMessage());
    }

    return "redirect:/user/community/join";
}

    @PostMapping("/join-request")
    @ResponseBody
    public ResponseEntity<?> createJoinRequest(
            @RequestParam("communityId") Long communityId,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        try {
            String result = communityService.createJoinRequest(communityId, user);
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating join request: " + e.getMessage());
        }
    }

    @GetMapping("/manage-requests")
    public String showManageRequestsPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<CommunityJoinRequest> joinRequests = communityService.getPendingRequestsForAdmin(user.getId());
        System.out.println("Fetched join requests for admin " + user.getId() + ": " + joinRequests.size());
        for (CommunityJoinRequest req : joinRequests) {
            System.out.println("Request: " + req.getId() + " | Community: " + req.getCommunity().getCommunityName() + " | User: " + req.getUser().getFullName() + " | Status: " + req.getStatus());
        }
        model.addAttribute("joinRequests", joinRequests);
        return "manage-requests";
    }
    
    @PostMapping("/approve-request")
    public String approveRequest(
            @RequestParam("requestId") Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        try {
            String result = communityService.approveRequest(requestId, user.getId());
            redirectAttributes.addFlashAttribute("manageMsg", result);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("manageMsg", "Error: " + e.getMessage());
        }
        
        return "redirect:/user/community/my-communities?tab=manage-requests";
    }

    @PostMapping("/reject-request")
    public String rejectRequest(
            @RequestParam("requestId") Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        try {
            String result = communityService.rejectRequest(requestId, user.getId());
            redirectAttributes.addFlashAttribute("manageMsg", result);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("manageMsg", "Error: " + e.getMessage());
        }
        
        return "redirect:/user/community/my-communities?tab=manage-requests";
    }


}
