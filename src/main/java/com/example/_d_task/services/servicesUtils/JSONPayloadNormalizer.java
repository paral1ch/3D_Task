package com.example._d_task.services.servicesUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class JSONPayloadNormalizer {
    private final ObjectMapper objectMapper = new ObjectMapper();
    protected JsonNode normalizePayload(JsonNode payload) {
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

    protected boolean isCorruptedPayloadDescriptor(JsonNode payload) {
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
