package com.todoapp.data.repository;

import com.todoapp.data.model.Task;
import com.todoapp.data.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOwnerOrderByDeadlineAsc(User owner);
}