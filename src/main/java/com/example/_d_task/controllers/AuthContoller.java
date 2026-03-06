package com.example._d_task.controllers;


import com.example._d_task.DTO.RegisterDTO;
import com.example._d_task.Services.UserService;
import com.example._d_task.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/auth")
public class AuthContoller {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostMapping(path="/register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO registerDTO) {
        


        userService.createUserFromRegister(registerDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body("Complete");
    }
}
