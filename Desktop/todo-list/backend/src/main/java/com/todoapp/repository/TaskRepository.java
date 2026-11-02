package com.todoapp.repository;

import com.todoapp.entity.Task;
import com.todoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOwnerOrderByDeadlineAsc(User owner);
}