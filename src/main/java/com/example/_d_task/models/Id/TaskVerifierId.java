package com.example._d_task.models.Id;


import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class TaskVerifierId {

    private Integer user;

    private Integer task;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskVerifierId that = (TaskVerifierId) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(task, that.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, task);
    }

}
