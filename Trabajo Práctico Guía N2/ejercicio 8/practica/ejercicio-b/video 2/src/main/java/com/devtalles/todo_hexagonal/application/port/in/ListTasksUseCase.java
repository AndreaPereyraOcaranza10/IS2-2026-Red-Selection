package com.devtalles.todo_hexagonal.application.port.in;

import com.devtalles.todo_hexagonal.domain.model.Task;
import java.util.List;

public interface ListTasksUseCase {
    List<Task> listAll();
}