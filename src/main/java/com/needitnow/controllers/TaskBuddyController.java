package com.needitnow.controllers;

import com.needitnow.entity.Community;
import com.needitnow.entity.Task;
import com.needitnow.entity.TaskOffer;
import com.needitnow.entity.User;
import com.needitnow.services.CommunityService;
import com.needitnow.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Collections;
import java.util.Comparator;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
public class TaskBuddyController {
    @Autowired
    private CommunityService communityService;
    @Autowired
    private TaskService taskService;
    
    // Convenience mapping for easy access to TaskBuddy from community page
    @GetMapping("/user/community/{communityId}/taskbuddy")
    public String redirectToTaskBuddy(@PathVariable("communityId") Long communityId) {
        return "redirect:/user/community/" + communityId + "/task-buddy";
    }
    
    @GetMapping("/user/community/{communityId}/taskbuddy/browse-tasks")
    public String showBrowseTasksPage(@PathVariable("communityId") Long communityId, 
                                    @RequestParam(value = "category", required = false) String category,
                                    @RequestParam(value = "reward", required = false) Boolean reward,
                                    @RequestParam(value = "dueToday", required = false) Boolean dueToday,
                                    @RequestParam(value = "priority", required = false) String priority,
                                    @RequestParam(value = "duration", required = false) String duration,
                                    Model model, 
                                    HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        Community community = communityService.getCommunityById(communityId).orElse(null);
        if (community == null) {
            return "redirect:/user/community";
        }
        
        // Get all open tasks in the community (except the ones created by the current user)
        Community communityObj = new Community();
        communityObj.setId(communityId);
        List<Task> tasks = taskService.getOpenTasksInCommunity(communityObj)
            .stream()
            .filter(task -> !task.getCreator().getId().equals(user.getId()))
            .collect(Collectors.toList());
        
        // Apply filters
        if (category != null && !category.isEmpty()) {
            tasks = tasks.stream()
                    .filter(task -> category.equals(task.getCategory()))
                    .collect(Collectors.toList());
        }
        
        if (reward != null) {
            tasks = tasks.stream()
                    .filter(task -> (reward && task.getReward() != null && !task.getReward().isEmpty() && !task.getReward().equals("0")) ||
                                  (!reward && (task.getReward() == null || task.getReward().isEmpty() || task.getReward().equals("0"))))
                    .collect(Collectors.toList());
        }
        
        if (dueToday != null && dueToday) {
            LocalDate today = LocalDate.now();
            tasks = tasks.stream()
                    .filter(task -> task.getDueDate() != null && task.getDueDate().isEqual(today))
                    .collect(Collectors.toList());
        }
        
        if (priority != null && !priority.isEmpty()) {
            tasks = tasks.stream()
                    .filter(task -> priority.equalsIgnoreCase(task.getPriority()))
                    .collect(Collectors.toList());
        }
        
        if (duration != null && !duration.isEmpty()) {
            tasks = tasks.stream()
                    .filter(task -> task.getEstimatedDuration() != null && 
                                  task.getEstimatedDuration().equals(duration))
                    .collect(Collectors.toList());
        }
        
        // Get available categories, priorities, and durations for filters
        List<String> categories = tasks.stream()
            .map(Task::getCategory)
            .filter(cat -> cat != null && !cat.isEmpty())
            .distinct()
            .collect(Collectors.toList());
            
        // If no tasks found, get some sample categories
        if (categories.isEmpty()) {
            categories = Arrays.asList("Errands", "Household", "Gardening", "Pet Care", "Shopping");
        }
        
        // Create display names (capitalize first letter, replace underscores with spaces)
        List<String> displayNames = categories.stream()
            .map(cat -> {
                if (cat == null || cat.isEmpty()) return "";
                return cat.substring(0, 1).toUpperCase() + 
                       cat.substring(1).toLowerCase().replace("_", " ");
            })
            .collect(Collectors.toList());
            
        List<String> priorities = Arrays.asList("LOW", "MEDIUM", "HIGH");
        List<String> durations = Arrays.asList("15 min", "30 min", "1 hour", "2 hours", "3+ hours");
        
        model.addAttribute("browseTasks", tasks);
        model.addAttribute("community", community);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryDisplayNames", displayNames);
        model.addAttribute("priorities", priorities);
        model.addAttribute("durations", durations);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedDuration", duration);
        model.addAttribute("showRewardOnly", reward);
        model.addAttribute("showDueTodayOnly", dueToday);
        
        return "browse-tasks";
    }
    
    @GetMapping("/user/community/{communityId}/taskbuddy/my-requests")
    public String showMyRequestsPage(@PathVariable("communityId") Long communityId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        Community community = communityService.getCommunityById(communityId).orElse(null);
        if (community == null) {
            return "redirect:/user/home";
        }
        
        // Get all tasks created by the current user in this community
        List<Task> myTasks = taskService.getUserTasksInCommunity(community, user);
        
        // Ensure myTasks is never null
        if (myTasks == null) {
            myTasks = new ArrayList<>();
            System.out.println("myTasks was null, initialized to empty list");
        } else {
            System.out.println("Found " + myTasks.size() + " tasks for user " + user.getId() + " in community " + communityId);
        }
        
        model.addAttribute("communityId", communityId);
        model.addAttribute("communityName", community.getCommunityName());
        model.addAttribute("myTasks", myTasks);
        
        // Debug: Print model attributes
        System.out.println("Model attributes:");
        System.out.println("  communityId: " + communityId);
        System.out.println("  communityName: " + community.getCommunityName());
        System.out.println("  myTasks size: " + myTasks.size());
        
        // Add categories for the filter dropdown
        model.addAttribute("categories", List.of("Errands", "Household", "Moving", "Pet Care", "Delivery", "Other"));
        
        return "taskbuddy-my-requests";
    }

    @GetMapping("/user/community/{communityId}/task-buddy")
    public String showTaskBuddyPage(@PathVariable("communityId") Long communityId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        Community community = communityService.getCommunityById(communityId).orElse(null);
        if (community == null) {
            model.addAttribute("error", "Community not found");
            return "redirect:/user/home";
        }
        
        // Fetch high-priority open tasks
        List<Task> openTasks = taskService.getHighPriorityOpenTasksInCommunity(community);
        System.out.println("TaskBuddy: Fetched " + (openTasks != null ? openTasks.size() : 0) + " high-priority open tasks for community " + communityId);
        
        // Fetch other task lists
        List<Task> myTasks = taskService.getUserTasksInCommunity(community, user);
        List<Task> helpingTasks = taskService.getHelpingTasksInCommunity(community, user);
        List<Task> completedTasks = taskService.getCompletedTasksInCommunity(community);
        
        // Ensure lists are not null
        openTasks = openTasks != null ? openTasks : new ArrayList<>();
        myTasks = myTasks != null ? myTasks : new ArrayList<>();
        helpingTasks = helpingTasks != null ? helpingTasks : new ArrayList<>();
        completedTasks = completedTasks != null ? completedTasks : new ArrayList<>();
        
        // Add attributes to model
        model.addAttribute("user", user);
        model.addAttribute("communityId", communityId);
        model.addAttribute("communityName", community.getCommunityName());
        model.addAttribute("openTasks", openTasks);
        model.addAttribute("myTasks", myTasks);
        model.addAttribute("helpingTasks", helpingTasks);
        model.addAttribute("completedTasks", completedTasks);
        
        // Debug: Print model attributes
        System.out.println("TaskBuddy Model attributes:");
        System.out.println("  communityId: " + communityId);
        System.out.println("  communityName: " + community.getCommunityName());
        System.out.println("  openTasks size: " + openTasks.size());
        System.out.println("  myTasks size: " + myTasks.size());
        System.out.println("  helpingTasks size: " + helpingTasks.size());
        System.out.println("  completedTasks size: " + completedTasks.size());
        
        return "task-buddy";
    }

    @GetMapping("/user/community/{communityId}/browse-tasks")
    public String showBrowseTasksPage(@PathVariable("communityId") Long communityId, Model model, HttpSession session,
                                      @RequestParam(value = "category", required = false) String category,
                                      @RequestParam(value = "reward", required = false) Boolean reward,
                                      @RequestParam(value = "dueToday", required = false) Boolean dueToday,
                                      @RequestParam(value = "priority", required = false) String priority,
                                      @RequestParam(value = "duration", required = false) String duration) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        Community community = communityService.getCommunityById(communityId).orElse(null);
        if (community == null) {
            model.addAttribute("error", "Community not found");
            return "redirect:/user/home";
        }
        
        // Fetch all open tasks
        List<Task> browseTasks = taskService.getOpenTasksInCommunity(community);
        System.out.println("BrowseTasks: Fetched " + (browseTasks != null ? browseTasks.size() : 0) + " open tasks for community " + communityId);
        
        // Apply filters
        if (browseTasks != null) {
            if (category != null && !category.isEmpty()) {
                browseTasks = browseTasks.stream().filter(t -> category.equals(t.getCategory())).toList();
            }
            if (reward != null) {
                if (reward) {
                    browseTasks = browseTasks.stream().filter(Task::isHasReward).toList();
                } else {
                    browseTasks = browseTasks.stream().filter(task -> !task.isHasReward()).toList();
                }
            }
            if (dueToday != null && dueToday) {
                browseTasks = browseTasks.stream()
                        .filter(t -> t.getDueDate() != null && t.getDueDate().isEqual(LocalDate.now()))
                        .toList();
            }
            if (priority != null && !priority.isEmpty()) {
                browseTasks = browseTasks.stream()
                        .filter(t -> priority.equalsIgnoreCase(t.getPriority()))
                        .toList();
            }
            if (duration != null && !duration.isEmpty()) {
                Map<String, Integer> durationValues = new java.util.HashMap<>();
                durationValues.put("15min", 15);
                durationValues.put("30min", 30);
                durationValues.put("1hour", 60);
                durationValues.put("2hours", 120);
                durationValues.put("half-day", 240);
                durationValues.put("full-day", 480);
                durationValues.put("15 min", 15);
                durationValues.put("30 min", 30);
                durationValues.put("1 hour", 60);
                durationValues.put("2 hour", 120);
                durationValues.put("2 hours", 120);
                durationValues.put("60min", 60);
                durationValues.put("120min", 120);
                
                Integer selectedDurationValue = durationValues.get(duration);
                System.out.println("Selected duration: " + duration + " -> " + selectedDurationValue + " minutes");
                
                if (selectedDurationValue != null) {
                    final int finalSelectedValue = selectedDurationValue;
                    browseTasks = browseTasks.stream().filter(task -> {
                        String taskDuration = task.getEstimatedDuration();
                        if (taskDuration == null || taskDuration.isEmpty()) {
                            return false;
                        }
                        Integer taskDurationValue = durationValues.get(taskDuration);
                        if (taskDurationValue == null) {
                            for (Map.Entry<String, Integer> entry : durationValues.entrySet()) {
                                if (entry.getKey().equalsIgnoreCase(taskDuration)) {
                                    taskDurationValue = entry.getValue();
                                    break;
                                }
                            }
                        }
                        System.out.println("Task: " + task.getTitle() + ", Duration: " + taskDuration + 
                                         " -> " + taskDurationValue + " minutes, Include? " + 
                                         (taskDurationValue != null && taskDurationValue <= finalSelectedValue));
                        return taskDurationValue != null && taskDurationValue <= finalSelectedValue;
                    }).toList();
                }
            }
        } else {
            browseTasks = new ArrayList<>();
        }
        
        // Add filter options to the model - match categories with the create task form
        List<String> categories = List.of("pickup", "petcare", "elderly", "tech", "tutoring", "urgent", "others");
        List<String> categoryDisplayNames = List.of("Pickup & Delivery", "Pet Care", "Elderly Help", "Tech Help", "Tutoring", "Urgent Assistance", "Others");
        List<String> priorities = List.of("HIGH", "MEDIUM", "LOW");
        List<String> durations = List.of("15min", "30min", "1hour", "2hours", "half-day", "full-day");
        
        // Fetch other task lists for tabs
        List<Task> myTasks = taskService.getUserTasksInCommunity(community, user);
        List<Task> helpingTasks = taskService.getHelpingTasksInCommunity(community, user);
        List<Task> completedTasks = taskService.getCompletedTasksInCommunity(community);
        
        // Ensure lists are not null
        myTasks = myTasks != null ? myTasks : new ArrayList<>();
        helpingTasks = helpingTasks != null ? helpingTasks : new ArrayList<>();
        completedTasks = completedTasks != null ? completedTasks : new ArrayList<>();
        
        model.addAttribute("user", user);
        model.addAttribute("communityId", communityId);
        model.addAttribute("communityName", community.getCommunityName());
        model.addAttribute("browseTasks", browseTasks); // Match the template's expected attribute name
        model.addAttribute("myTasks", myTasks);
        model.addAttribute("helpingTasks", helpingTasks);
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryDisplayNames", categoryDisplayNames);
        model.addAttribute("priorities", priorities);
        model.addAttribute("durations", durations);
        
        // Debug: Print model attributes
        System.out.println("BrowseTasks Model attributes:");
        System.out.println("  communityId: " + communityId);
        System.out.println("  communityName: " + community.getCommunityName());
        System.out.println("  openTasks size: " + browseTasks.size());
        System.out.println("  myTasks size: " + myTasks.size());
        System.out.println("  helpingTasks size: " + helpingTasks.size());
        System.out.println("  completedTasks size: " + completedTasks.size());
        
        return "browse-tasks";
    }

    @PostMapping("/user/community/{communityId}/task-buddy/create")
    public String createTask(@PathVariable("communityId") Long communityId,
                             @RequestParam("title") String title,
                             @RequestParam("description") String description,
                             @RequestParam("dueDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                             @RequestParam("timeNeeded") String timeNeeded,
                             @RequestParam("category") String category,
                             @RequestParam("priority") String priority,
                             @RequestParam(value = "location", required = false) String location,
                             @RequestParam("estimatedDuration") String estimatedDuration,
                             @RequestParam(value = "reward", required = false) String reward,
                             @RequestParam(value = "hasReward", defaultValue = "false") boolean hasReward,
                             @RequestParam(value = "taskImage", required = false) MultipartFile taskImage,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        Community community = communityService.getCommunityById(communityId).orElse(null);
        if (community == null) {
            redirectAttributes.addFlashAttribute("error", "Community not found");
            return "redirect:/user/home";
        }
        
        String imagePath = null;
        if (taskImage != null && !taskImage.isEmpty()) {
            try {
                String filename = System.currentTimeMillis() + "_" + taskImage.getOriginalFilename();
                Path uploadPath = Paths.get("uploads", "tasks");
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                Path filePath = uploadPath.resolve(filename);
                Files.copy(taskImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                imagePath = "/uploads/tasks/" + filename;
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
            }
        }
        
        // Backend validation for required fields
        if (title == null || title.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Task title is required.");
            return "redirect:/user/community/" + communityId + "/task-buddy";
        }
        if (description == null || description.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Task description is required.");
            return "redirect:/user/community/" + communityId + "/task-buddy";
        }
        if ((hasReward && (reward == null || reward.trim().isEmpty())) || (!hasReward && reward != null && !reward.trim().isEmpty())) {
            redirectAttributes.addFlashAttribute("error", "Please enter a reward amount or select 'No reward'.");
            return "redirect:/user/community/" + communityId + "/task-buddy";
        }
        
        taskService.createTask(title, description, dueDate, timeNeeded, 
                              category, priority, location, estimatedDuration, 
                              reward, hasReward, imagePath, 
                              user, community);
                              
        redirectAttributes.addFlashAttribute("success", "Task posted successfully!");
        return "redirect:/user/community/" + communityId + "/task-buddy";
    }

    @PostMapping("/user/community/{communityId}/task-buddy/offer-help")
    public String offerHelp(@PathVariable("communityId") Long communityId,
                            @RequestParam("taskId") Long taskId,
                            @RequestParam(value = "message", required = false) String message,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            redirectAttributes.addFlashAttribute("error", "Task not found");
            return "redirect:/user/community/" + communityId + "/task-buddy";
        }
        if (task.getCreator().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "You cannot offer help for your own task");
            return "redirect:/user/community/" + communityId + "/task-buddy";
        }
        TaskOffer offer = taskService.offerHelp(task, user, message);
        if (offer == null) {
            redirectAttributes.addFlashAttribute("error", "This task already has a helper or is completed");
            return "redirect:/user/community/" + communityId + "/task-buddy";
        }
        redirectAttributes.addFlashAttribute("success", "Your offer to help has been sent!");
        return "redirect:/user/community/" + communityId + "/task-buddy";
    }

    @GetMapping("/user/api/tasks/{taskId}/responses")
    @ResponseBody
    public ResponseEntity<?> getTaskOffers(@PathVariable("taskId") Long taskId) {
        try {
            System.out.println("Fetching offers for task ID: " + taskId);
            List<TaskOffer> offers = taskService.getOffersForTask(taskId);
            System.out.println("Found " + offers.size() + " offers for task ID: " + taskId);
            return ResponseEntity.ok(offers);
        } catch (Exception e) {
            System.err.println("Error fetching offers for task " + taskId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error fetching task offers"));
        }
    }

    @PostMapping("/user/community/{communityId}/task-buddy/accept-offer")
    public String acceptOffer(@PathVariable("communityId") Long communityId,
                              @RequestParam("taskId") Long taskId,
                              @RequestParam("offerId") Long offerId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            redirectAttributes.addFlashAttribute("error", "Task not found");
            return "redirect:/user/community/" + communityId + "/task-buddy";
        }
        if (!task.getCreator().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Only the task creator can accept offers");
            return "redirect:/user/community/" + communityId + "/task-buddy";
        }
        boolean success = taskService.acceptOffer(task, offerId);
        if (!success) {
            redirectAttributes.addFlashAttribute("error", "Cannot accept this offer");
            return "redirect:/user/community/" + communityId + "/task-buddy";
        }
        redirectAttributes.addFlashAttribute("success", "Helper selected successfully!");
        return "redirect:/user/community/" + communityId + "/task-buddy";
    }

    @PostMapping("/user/community/{communityId}/task-buddy/complete-task")
    @ResponseBody
    public ResponseEntity<?> completeTask(@PathVariable("communityId") Long communityId,
                                       @RequestParam("taskId") Long taskId,
                                       @RequestParam("offerId") Long offerId,
                                       HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "User not logged in"));
        }
        
        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Task not found"));
        }
        
        if (!task.getCreator().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Only the task creator can complete the task"));
        }
        
        try {
            taskService.completeTask(task, "Task completed by task creator");
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error completing task: " + e.getMessage()));
        }
    }

    @PostMapping("/user/community/{communityId}/task-buddy/delete")
    public String deleteTask(@PathVariable("communityId") Long communityId,
                             @RequestParam("taskId") Long taskId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            redirectAttributes.addFlashAttribute("error", "Task not found");
            return "redirect:/user/community/" + communityId + "/task-buddy";
        }
        if (!task.getCreator().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Only the task creator can delete it");
            return "redirect:/user/community/" + communityId + "/task-buddy";
        }
        boolean success = taskService.deleteTask(task);
        if (!success) {
            redirectAttributes.addFlashAttribute("error", "This task cannot be deleted");
            return "redirect:/user/community/" + communityId + "/task-buddy";
        }
        redirectAttributes.addFlashAttribute("success", "Task deleted successfully!");
        return "redirect:/user/community/" + communityId + "/task-buddy";
    }
    
    @GetMapping("/user/community/{communityId}/task-buddy/history")
    public String showTaskHistoryPage(@PathVariable("communityId") Long communityId,
                                   Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Community community = communityService.getCommunityById(communityId).orElse(null);
        if (community == null) {
            model.addAttribute("error", "Community not found");
            return "redirect:/user/home";
        }

        // Get completed tasks where user is either creator or helper
        List<Task> completedTasks = taskService.getCompletedTasksForUserInCommunity(community, user);
        
        // Get cancelled tasks where user is either creator or helper
        List<Task> cancelledTasks = taskService.getCancelledTasksForUserInCommunity(community, user);
        
        model.addAttribute("communityId", communityId);
        model.addAttribute("communityName", community.getCommunityName());
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("cancelledTasks", cancelledTasks);
        model.addAttribute("user", user);
        
        return "task-history";
    }
    
    @GetMapping("/user/community/{communityId}/taskbuddy/my-offers")
    public String showMyOffersPage(@PathVariable("communityId") Long communityId,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "category", required = false) String category,
                              @RequestParam(value = "sort", required = false, defaultValue = "newest") String sort,
                              Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Community community = communityService.getCommunityById(communityId).orElse(null);
        if (community == null) {
            return "redirect:/user/home";
        }

        // Fetch user offers
        List<TaskOffer> offers = taskService.getUserOffersInCommunity(community, user);

        // Apply filters
        if (status != null && !status.isEmpty()) {
            if (status.equals("completed")) {
                offers = offers.stream()
                        .filter(offer -> offer.getTask().isCompleted())
                        .collect(Collectors.toList());
            } else {
                offers = offers.stream()
                        .filter(offer -> status.equalsIgnoreCase(offer.getStatus()))
                        .collect(Collectors.toList());
            }
        }

        if (category != null && !category.isEmpty()) {
            offers = offers.stream()
                    .filter(offer -> category.equals(offer.getTask().getCategory()))
                    .collect(Collectors.toList());
        }

        // Sort offers
        if ("oldest".equalsIgnoreCase(sort)) {
            offers.sort(Comparator.comparing(TaskOffer::getCreatedAt));
        } else {
            offers.sort(Comparator.comparing(TaskOffer::getCreatedAt, Comparator.reverseOrder()));
        }

        // Categories for filter
        List<String> categories = List.of("pickup", "petcare", "elderly", "tech", "tutoring", "urgent", "others");
        List<String> categoryDisplayNames = List.of("Pickup & Delivery", "Pet Care", "Elderly Help", "Tech Help", "Tutoring", "Urgent Assistance", "Other");

        model.addAttribute("myOffers", offers);
        model.addAttribute("communityId", communityId);
        model.addAttribute("communityName", community.getCommunityName());
        model.addAttribute("categories", categories);
        model.addAttribute("categoryDisplayNames", categoryDisplayNames);

        return "my-offers";
    }

    @PostMapping("/user/community/{communityId}/task-buddy/cancel-offer")
    public String cancelOffer(@PathVariable("communityId") Long communityId,
                             @RequestParam("offerId") Long offerId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        TaskOffer offer = taskService.getOfferById(offerId);
        if (offer == null || !offer.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Invalid offer");
            return "redirect:/user/community/" + communityId + "/taskbuddy/my-offers";
        }

        if (!offer.getStatus().equals("pending")) {
            redirectAttributes.addFlashAttribute("error", "Only pending offers can be canceled");
            return "redirect:/user/community/" + communityId + "/taskbuddy/my-offers";
        }

        taskService.cancelOffer(offer);
        redirectAttributes.addFlashAttribute("success", "Offer canceled successfully!");
        return "redirect:/user/community/" + communityId + "/taskbuddy/my-offers";
    }

    @PostMapping("/user/community/{communityId}/task-buddy/mark-helped")
    public String markHelped(@PathVariable("communityId") Long communityId,
                             @RequestParam("taskId") Long taskId,
                             @RequestParam("offerId") Long offerId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Task task = taskService.getTaskById(taskId);
        TaskOffer offer = taskService.getOfferById(offerId);
        if (task == null || offer == null || !offer.getUser().getId().equals(user.getId()) ||
            (task.getHelper() == null || !task.getHelper().getId().equals(user.getId()))) {
            redirectAttributes.addFlashAttribute("error", "Invalid task or offer");
            return "redirect:/user/community/" + communityId + "/taskbuddy/my-offers";
        }

        if (task.isCompleted()) {
            redirectAttributes.addFlashAttribute("error", "Task is already completed");
            return "redirect:/user/community/" + communityId + "/taskbuddy/my-offers";
        }

        taskService.completeTask(task, "Task marked helped by helper");
        redirectAttributes.addFlashAttribute("success", "Task marked as helped successfully!");
        return "redirect:/user/community/" + communityId + "/taskbuddy/my-offers";
    }
}