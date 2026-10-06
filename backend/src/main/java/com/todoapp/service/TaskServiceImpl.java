package com.todoapp.service;

import com.todoapp.data.model.Task;
import com.todoapp.data.enums.TaskStatus;
import com.todoapp.data.model.User;
import com.todoapp.data.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    public Task createTask(String title, User user, LocalDateTime deadline) {
        Task task = new Task();
        task.setOwner(user);
        task.setTitle(title);
        task.setDeadline(deadline);
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public void markTaskAsCompleted(Task task) {
        if (task.getStatus() != TaskStatus.COMPLETED){
            task.setStatus(TaskStatus.COMPLETED);
            taskRepository.save(task);
        }
    }

    @Override
    public List<Task> viewAllTasks(User user) {
        return taskRepository.findByOwnerOrderByDeadlineAsc(user);
    }

    @Override
    public void removeTask(Task task) {
        taskRepository.delete(task);
    }

    @Override
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));
    }
}
