package com.devtalles.todo_hexagonal.application.port.out;

import com.devtalles.todo_hexagonal.domain.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepositoryPort {
    Task save(Task task);
    List<Task> findAll();
    Optional<Task> findById(Long id);
}
