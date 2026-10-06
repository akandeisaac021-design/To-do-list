package com.todoapp.dto.response;

import com.todoapp.data.enums.TaskStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CreateTaskResponse {
    private Long id;
    private String title;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private TaskStatus status;
}