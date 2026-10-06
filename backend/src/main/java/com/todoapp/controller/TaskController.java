package com.todoapp.controller;

import com.todoapp.dto.request.CreateTaskRequest;
import com.todoapp.dto.response.CompleteTaskResponse;
import com.todoapp.dto.response.CreateTaskResponse;
import com.todoapp.data.model.Task;
import com.todoapp.data.model.User;
import com.todoapp.service.CurrentUserService;
import com.todoapp.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<CreateTaskResponse> createTask(
            @RequestBody @Valid CreateTaskRequest request, HttpServletRequest httpRequest) {
        User currentUser = currentUserService.getCurrentUser(httpRequest);
        Task task = taskService.createTask(request.getTitle(), currentUser, request.getDeadline());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(task));
    }

    @GetMapping
    public ResponseEntity<List<CreateTaskResponse>> getAllTasks(HttpServletRequest httpRequest) {
        User currentUser = currentUserService.getCurrentUser(httpRequest);
        return ResponseEntity.ok(
                taskService.viewAllTasks(currentUser).stream().map(this::toResponse).toList());
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<CompleteTaskResponse> completeTask(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        taskService.markTaskAsCompleted(task);
        return ResponseEntity.ok(
                CompleteTaskResponse.builder().id(task.getId()).status(task.getStatus()).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.removeTask(taskService.getTaskById(id));
        return ResponseEntity.noContent().build();
    }

    private CreateTaskResponse toResponse(Task task) {
        return CreateTaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .deadline(task.getDeadline())
                .createdAt(task.getCreatedAt())
                .status(task.getStatus())
                .build();
    }
}
