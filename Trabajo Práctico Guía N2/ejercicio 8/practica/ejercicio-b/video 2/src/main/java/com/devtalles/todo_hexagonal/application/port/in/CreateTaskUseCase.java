package com.devtalles.todo_hexagonal.application.port.in;

import com.devtalles.todo_hexagonal.domain.model.Task;

public interface CreateTaskUseCase {
    Task create(Task task);
}
