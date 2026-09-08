package com.example._d_task.services;


import com.example._d_task.enums.TaskEventType;
import com.example._d_task.models.TaskEventModel;
import com.example._d_task.repositories.ProjectRepository;
import com.example._d_task.repositories.TaskEventRepository;
import com.example._d_task.repositories.TaskRepository;
import com.example._d_task.repositories.UserRepository;
import com.example._d_task.services.servicesUtils.ModelToDTOConverters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TaskEventService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final TaskEventRepository eventRepository;

    private final TaskServices taskServices;

    private final UserService userService;

    private final ProjectRepository projectRepository;
    private final ModelToDTOConverters modelToDTOConverters;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TaskEventService(TaskRepository taskRepository,UserRepository userRepository,TaskEventRepository eventRepository,
                            TaskServices taskServices, UserService userService,ProjectRepository projectRepository,
                            ModelToDTOConverters modelToDTOConverters){
        this.taskServices = taskServices;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.projectRepository = projectRepository;
        this.modelToDTOConverters = modelToDTOConverters;
    }

    public void record(Integer task_id, TaskEventType eventType, Integer user_id, ObjectNode payload, Integer project_id) {
        TaskEventModel event = new TaskEventModel();
        event.setEvent_type(eventType);
        event.setTask(taskRepository.findById(task_id));
        event.setUser(userRepository.findByIdNullable(user_id));
        event.setPayload(normalizePayload(payload));
        event.setCreated_at(LocalDateTime.now());
        event.setProject(projectRepository.findByProjectId(project_id));
        eventRepository.save(event);
    }



    private JsonNode normalizePayload(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return null;
        }
        if (!payload.isTextual()) {
            return payload;
        }
        String raw = payload.asText();
        if (raw == null) {
            return payload;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return payload;
        }
        try {
            return objectMapper.readTree(trimmed);
        } catch (Exception ignored) {
            return payload;
        }
    }

    private boolean isCorruptedPayloadDescriptor(JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            return false;
        }
        if (
            !payload.has("nodeType") ||
            !payload.has("array") ||
            !payload.has("object") ||
            !payload.has("valueNode")
        ) {
            return false;
        }
        int markerCount = 0;
        String[] markerKeys = {
            "array",
            "bigDecimal",
            "bigInteger",
            "binary",
            "boolean",
            "containerNode",
            "double",
            "float",
            "floatingPointNumber",
            "int",
            "integralNumber",
            "long",
            "missingNode",
            "nodeType",
            "null",
            "number",
            "object",
            "pojo",
            "short",
            "textual",
            "valueNode",
        };
        for (String markerKey : markerKeys) {
            if (payload.has(markerKey)) {
                markerCount++;
            }
        }
        return markerCount >= 8;
    }

}
