package com.devtalles.todo_hexagonal.application.port.in;

import com.devtalles.todo_hexagonal.domain.model.Task;

public interface GetTaskUseCase {
    Task getById(Long id);
}