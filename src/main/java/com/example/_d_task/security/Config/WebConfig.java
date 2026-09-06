package com.example._d_task.security.Config;

import com.example._d_task.security.interceptors.ProjectAccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private ProjectAccessInterceptor pai;

    public WebConfig(ProjectAccessInterceptor pai){
        this.pai = pai;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        HandlerInterceptor projectAccessInterceptor;
        registry.addInterceptor(pai)
                .addPathPatterns("/project/*/task/**");
    }
}
