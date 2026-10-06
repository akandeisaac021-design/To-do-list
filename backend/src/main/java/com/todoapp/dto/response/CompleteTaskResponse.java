package com.todoapp.dto.response;

import com.todoapp.data.enums.TaskStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CompleteTaskResponse {
    private Long id;
    private TaskStatus status;
}