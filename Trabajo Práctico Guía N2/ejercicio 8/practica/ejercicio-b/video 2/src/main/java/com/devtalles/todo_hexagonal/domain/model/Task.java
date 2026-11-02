package com.devtalles.todo_hexagonal.domain.model;

import lombok.*;

import java.time.LocalDateTime;
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {
    @EqualsAndHashCode.Include
    private Long id;

    private String title;
    private String description;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public void complete() {
        if (this.status == TaskStatus.COMPLETED) {
            throw new IllegalStateException("La tarea ya está completada");
        }
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void reopen() {
        if (this.status == TaskStatus.PENDING) {
            throw new IllegalStateException("La tarea ya está pendiente");
        }
        this.status = TaskStatus.PENDING;
        this.completedAt = null;
    }

    public void initDefaults() {
        if (status == null) status = TaskStatus.PENDING;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }


}














