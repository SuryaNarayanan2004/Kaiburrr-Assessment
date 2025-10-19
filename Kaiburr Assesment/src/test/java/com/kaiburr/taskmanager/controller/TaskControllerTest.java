package com.kaiburr.taskmanager.controller;

import com.kaiburr.taskmanager.model.Task;
import com.kaiburr.taskmanager.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void getAll_returnsEmptyArray() throws Exception {
        when(taskService.getAll()).thenReturn(List.of());
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getById_notFound() throws Exception {
        when(taskService.getById("x")).thenReturn(Optional.empty());
        mockMvc.perform(get("/tasks").param("id", "x"))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_upsert_ok() throws Exception {
        Task t = new Task();
        t.setId("1"); t.setName("n"); t.setOwner("o"); t.setCommand("echo hi");
        when(taskService.upsert(any(Task.class))).thenReturn(t);
        mockMvc.perform(put("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"1\",\"name\":\"n\",\"owner\":\"o\",\"command\":\"echo hi\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }
}


