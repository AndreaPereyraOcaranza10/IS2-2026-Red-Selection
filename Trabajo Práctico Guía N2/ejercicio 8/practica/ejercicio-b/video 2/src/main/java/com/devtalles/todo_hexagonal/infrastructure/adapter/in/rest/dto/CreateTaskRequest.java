package com.devtalles.todo_hexagonal.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTaskRequest {

    @NotBlank(message = "El título es obligatorio")
    private String title;

    private String description;
}
