package com.todoapp.service;

import com.todoapp.data.model.Task;
import com.todoapp.data.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {

    Task createTask(String title, User user, LocalDateTime deadline);

    void markTaskAsCompleted(Task task);

    List<Task> viewAllTasks(User user);

    void removeTask(Task task);

    Task getTaskById(Long id);

}
