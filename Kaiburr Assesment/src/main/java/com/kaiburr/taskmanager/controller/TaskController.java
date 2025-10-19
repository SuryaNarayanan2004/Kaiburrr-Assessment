package com.kaiburr.taskmanager.controller;

import com.kaiburr.taskmanager.model.Task;
import com.kaiburr.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<?> getTasks(@RequestParam(value = "id", required = false) String id) {
        if (id != null && !id.isBlank()) {
            Optional<Task> task = taskService.getById(id);
            return task.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found"));
        }
        List<Task> all = taskService.getAll();
        return ResponseEntity.ok(all);
    }

    @PutMapping
    public ResponseEntity<Task> upsert(@Valid @RequestBody Task task) {
        Task saved = taskService.upsert(task);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        taskService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Task>> search(@RequestParam("name") String namePart) {
        return ResponseEntity.ok(taskService.searchByName(namePart));
    }

    @PutMapping("/{id}/execute")
    public ResponseEntity<Task> execute(@PathVariable String id) {
        Task updated = taskService.execute(id);
        return ResponseEntity.ok(updated);
    }
}


