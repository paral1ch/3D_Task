package com.example._d_task.ENUMS;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class ProjectRolePermissions {

    public static final Set<ProjectRoles> EDIT_PROJECT =
            EnumSet.of(ProjectRoles.CREATOR);


    public static boolean canModify(List<ProjectRoles> roles){
        return roles.stream().anyMatch(EDIT_PROJECT::contains);
    }
}
