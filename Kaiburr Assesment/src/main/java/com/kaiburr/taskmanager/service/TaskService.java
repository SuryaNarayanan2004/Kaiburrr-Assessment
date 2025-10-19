package com.kaiburr.taskmanager.service;

import com.kaiburr.taskmanager.model.Task;
import com.kaiburr.taskmanager.model.TaskExecution;
import com.kaiburr.taskmanager.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final CommandValidator commandValidator;

    public TaskService(TaskRepository taskRepository, CommandValidator commandValidator) {
        this.taskRepository = taskRepository;
        this.commandValidator = commandValidator;
    }

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public Optional<Task> getById(String id) {
        return taskRepository.findById(id);
    }

    public List<Task> searchByName(String namePart) {
        if (!StringUtils.hasText(namePart)) {
            return getAll();
        }
        return taskRepository.findByNameContainingIgnoreCase(namePart);
    }

    public Task upsert(Task task) {
        if (!commandValidator.isSafe(task.getCommand())) {
            throw new IllegalArgumentException("Unsupported or unsafe command. Only echo ... is allowed.");
        }
        return taskRepository.save(task);
    }

    public void deleteById(String id) {
        taskRepository.deleteById(id);
    }

    public Task execute(String id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
        if (!commandValidator.isSafe(task.getCommand())) {
            throw new IllegalArgumentException("Stored command is unsafe. Only echo ... is allowed.");
        }

        Date start = new Date();
        String output;
        try {
            // Execute as a shell command safely limited to echo
            ProcessBuilder pb = new ProcessBuilder();
            if (isWindows()) {
                pb.command("cmd.exe", "/c", task.getCommand());
            } else {
                pb.command("sh", "-c", task.getCommand());
            }
            Process process = pb.start();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            int exit = process.waitFor();
            output = sb.toString().trim();
            log.info("Executed task {} with exit code {}", id, exit);
        } catch (Exception e) {
            log.error("Failed to execute task {}", id, e);
            output = "ERROR: " + e.getMessage();
        }
        Date end = new Date();

        TaskExecution exec = new TaskExecution(start, end, output);
        task.getTaskExecutions().add(exec);
        return taskRepository.save(task);
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }
}


