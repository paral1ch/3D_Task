package com.example._d_task.services.servicesUtils;

import com.example._d_task.enums.TaskEnum;
import com.example._d_task.models.TaskModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class JSONPayloadUtils {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode normalizePayloadNode(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return null;
        }
        if (!payload.isTextual()) {
            return payload;
        }
        String raw = payload.asText();
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(trimmed);
        } catch (Exception ignored) {
            return null;
        }
    }

    public Integer readPayloadInt(JsonNode payload, String key) {
        JsonNode normalized = normalizePayloadNode(payload);
        if (normalized == null || !normalized.isObject()) {
            return null;
        }
        JsonNode value = normalized.get(key);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.canConvertToInt()) {
            return value.asInt();
        }
        if (value.isTextual()) {
            try {
                return Integer.parseInt(value.asText().trim());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    public String readPayloadString(JsonNode payload, String key) {
        JsonNode normalized = normalizePayloadNode(payload);
        if (normalized == null || !normalized.isObject()) {
            return null;
        }
        JsonNode value = normalized.get(key);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    public LocalDate readPayloadDate(JsonNode payload, String key) {
        String value = readPayloadString(payload, key);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
            return null;
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean isTaskOverdue(
            TaskModel task,
            LocalDate now,
            Map<Integer, LocalDateTime> doneAtByTask,
            Map<Integer, LocalDate> deadlineAtDoneByTask
    ) {
        if (task == null) {
            return false;
        }
        LocalDate deadline = task.getDeadline();
        Integer taskId = task.getTask_id();

        if (TaskEnum.DONE.equals(task.getStatus())) {
            LocalDateTime doneAt = doneAtByTask.get(taskId);
            if (doneAt == null && task.getDone_at() != null) {
                doneAt = task.getDone_at().atStartOfDay();
            }
            LocalDate deadlineAtDone = deadlineAtDoneByTask.get(taskId);
            if (deadlineAtDone != null) {
                deadline = deadlineAtDone;
            }
            if (deadline == null || doneAt == null) {
                return false;
            }
            return doneAt.toLocalDate().isAfter(deadline);
        }

        return deadline != null && deadline.isBefore(now);
    }
}
