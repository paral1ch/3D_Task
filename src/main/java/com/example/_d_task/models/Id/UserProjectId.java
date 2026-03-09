package com.example._d_task.models.Id;

import com.example._d_task.ENUMS.ProjectRoles;

import java.util.Objects;

public class UserProjectId {

    private Integer user_id;
    private Integer project_id;
    private ProjectRoles project_role;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProjectId that = (UserProjectId) o;
        return Objects.equals(user_id, that.user_id) &&
                Objects.equals(project_id, that.project_id) &&
                Objects.equals(project_role,that.project_role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_id, project_id,project_role);
    }

}
