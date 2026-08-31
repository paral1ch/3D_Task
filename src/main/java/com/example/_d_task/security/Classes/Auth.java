package com.example._d_task.security.Classes;

import com.example._d_task.models.UserModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class Auth {


    public static UserModel user(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserModel) {
            return (UserModel) principal;
        }
        return null;
    }



    public static boolean check(){
        return user()!=null;
    }
}
