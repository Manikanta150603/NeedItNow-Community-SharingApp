package com.needitnow.service;

import com.needitnow.entity.Community;
import com.needitnow.entity.Task;
import com.needitnow.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {
    Task createTask(String title, String description, LocalDate dueDate, String category, String priority, User creator, Community community);
    Task getTaskById(Long taskId);
    List<Task> getOpenTasksInCommunity(Community community);
    List<Task> getUserTasksInCommunity(Community community, User user);
    List<Task> getHelpingTasksInCommunity(Community community, User user);
    List<Task> getCompletedTasksInCommunity(Community community);
    boolean offerHelp(Task task, User helper, String helperNote);
    boolean completeTask(Task task, String feedback);
    boolean deleteTask(Task task);
}
