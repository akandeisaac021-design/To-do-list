package com.todoapp.dto.request;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateTaskRequest {
    private String title;
    private LocalDateTime deadline;
}
