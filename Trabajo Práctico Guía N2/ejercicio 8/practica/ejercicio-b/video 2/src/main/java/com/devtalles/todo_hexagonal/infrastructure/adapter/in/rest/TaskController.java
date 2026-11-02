package com.devtalles.todo_hexagonal.infrastructure.adapter.in.rest;

import com.devtalles.todo_hexagonal.application.port.in.CreateTaskUseCase;
import com.devtalles.todo_hexagonal.application.port.in.GetTaskUseCase;
import com.devtalles.todo_hexagonal.application.port.in.ListTasksUseCase;
import com.devtalles.todo_hexagonal.domain.model.Task;
import com.devtalles.todo_hexagonal.infrastructure.adapter.in.rest.dto.CreateTaskRequest;
import com.devtalles.todo_hexagonal.infrastructure.adapter.in.rest.dto.TaskResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final ListTasksUseCase listTasksUseCase;
    private final GetTaskUseCase getTaskUseCase;

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request){
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
        Task saved = createTaskUseCase.create(task);

        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(saved));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listAll() {
        List<TaskResponse> response = listTasksUseCase.listAll()
                .stream()
                .map(TaskResponse::from)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        Task task = getTaskUseCase.getById(id);
        return ResponseEntity.ok(TaskResponse.from(task));
    }
}
