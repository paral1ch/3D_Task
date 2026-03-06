package com.example._d_task.controllers;

import com.example._d_task.DTO.RegisterDTO;
import com.example._d_task.Services.UserService;
import com.example._d_task.models.UserModel;
import com.example._d_task.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/Users")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;


    @GetMapping(path = "/AllUsers")
    public @ResponseBody Iterable<UserModel> getUsers(){
        return userRepository.findAll();
    }




}
