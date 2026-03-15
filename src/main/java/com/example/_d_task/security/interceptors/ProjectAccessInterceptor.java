package com.example._d_task.security.interceptors;

import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.ProjectServices;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // Проверяем, что это вызов контроллера
        if (handler instanceof HandlerMethod) {
            String path = request.getRequestURI();
            // Проверяем, относится ли запрос к задачам проекта
            if (path.matches(".*/projects/\\d+/tasks.*")) {
                Integer projectId = extractProjectId(path);
                if (projectId == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID");
                    return false;
                }

                // Получаем текущего пользователя (например, из SecurityContext)
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return false;
                }

                // Проверяем, имеет ли пользователь доступ к проекту
                ProjectServices pr = new ProjectServices();
                if (!pr.userHasAccess(Auth.user().getUserId(),projectId)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "No access to this project");
                    return false;
                }
            }
        }
        return true; // разрешаем выполнение запроса
    }

    private Integer extractProjectId(String path) {
        // Простейший способ – регулярное выражение
        Pattern pattern = Pattern.compile("/project/(\\d+)/task");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }
}
