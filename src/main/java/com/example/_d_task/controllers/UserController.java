package com.example._d_task.controllers;

import com.example._d_task.Security.Classes.Auth;
import com.example._d_task.Services.UserService;
import com.example._d_task.models.UserModel;
import com.example._d_task.repositories.SessionRepository;
import com.example._d_task.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/Users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserService userService;


    @GetMapping(path = "/AllUsers")
    public @ResponseBody Iterable<UserModel> getUsers(){
        return userRepository.findAll();
    }

    @PostMapping(path = "/logout")
    public @ResponseBody String logout(@RequestHeader("Authorization") String header){
        String token = header.substring(7);
        sessionRepository.delete(sessionRepository.findByToken(token));
        return "Complete";
    }

    @GetMapping(path = "/profile")
    public @ResponseBody UserModel profile(){
        if(!Auth.check()){return null;}
        return Auth.user();
    }

    @GetMapping(path = "/userByToken")
    public @ResponseBody String test(@RequestHeader("Authorization") String header){
        String token = header.substring(7);
        return sessionRepository.findUserByToken(token).getUsername();
    }


}
