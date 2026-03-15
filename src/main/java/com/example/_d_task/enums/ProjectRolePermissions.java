package com.example._d_task.enums;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class ProjectRolePermissions {

    public static final Set<ProjectRoles> EDIT_PROJECT =
            EnumSet.of(ProjectRoles.CREATOR);

    public static final Set<ProjectRoles> CREATE_TASK = EnumSet.of(ProjectRoles.TASK_CREATOR,
            ProjectRoles.CREATOR);

    public static boolean canModifyProject(List<ProjectRoles> roles){
        return roles.stream().anyMatch(EDIT_PROJECT::contains);
    }

    public static boolean canCreateTask(List<ProjectRoles> roles){
        return roles.stream().anyMatch(CREATE_TASK::contains);
    }
}
