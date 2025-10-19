package com.kaiburr.taskmanager.service;

import com.kaiburr.taskmanager.model.Task;
import com.kaiburr.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TaskServiceTest {

    private TaskRepository taskRepository;
    private CommandValidator commandValidator;
    private TaskService taskService;

    @BeforeEach
    void setup() {
        taskRepository = Mockito.mock(TaskRepository.class);
        commandValidator = new CommandValidator();
        taskService = new TaskService(taskRepository, commandValidator);
    }

    @Test
    void upsert_rejectsUnsafeCommand() {
        Task t = new Task();
        t.setName("n");
        t.setOwner("o");
        t.setCommand("rm -rf /");
        assertThrows(IllegalArgumentException.class, () -> taskService.upsert(t));
    }

    @Test
    void upsert_acceptsEcho() {
        Task t = new Task();
        t.setName("n");
        t.setOwner("o");
        t.setCommand("echo hello");
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Task saved = taskService.upsert(t);
        assertThat(saved.getCommand()).isEqualTo("echo hello");
    }

    @Test
    void execute_appendsExecution() {
        Task t = new Task();
        t.setId("1");
        t.setName("n");
        t.setOwner("o");
        t.setCommand("echo test");
        when(taskRepository.findById("1")).thenReturn(Optional.of(t));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Task after = taskService.execute("1");
        assertThat(after.getTaskExecutions()).hasSize(1);
        assertThat(after.getTaskExecutions().get(0).getOutput()).contains("test");
    }
}


