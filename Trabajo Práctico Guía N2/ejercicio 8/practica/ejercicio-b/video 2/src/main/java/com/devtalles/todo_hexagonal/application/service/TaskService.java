package com.devtalles.todo_hexagonal.application.service;

import com.devtalles.todo_hexagonal.application.port.in.CreateTaskUseCase;
import com.devtalles.todo_hexagonal.application.port.in.GetTaskUseCase;
import com.devtalles.todo_hexagonal.application.port.in.ListTasksUseCase;
import com.devtalles.todo_hexagonal.application.port.out.TaskRepositoryPort;
import com.devtalles.todo_hexagonal.domain.exception.TaskNotFoundException;
import com.devtalles.todo_hexagonal.domain.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService implements CreateTaskUseCase, ListTasksUseCase, GetTaskUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    @Override
    public Task create(Task task) {
        return taskRepositoryPort.save(task);
    }

    @Override
    public List<Task> listAll() {
        return taskRepositoryPort.findAll();
    }

    @Override
    public Task getById(Long id) {
        return taskRepositoryPort.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}
