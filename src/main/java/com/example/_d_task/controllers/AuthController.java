package com.example._d_task.controllers;


import com.example._d_task.dto.LoginDTO;
import com.example._d_task.dto.RegisterDTO;
import com.example._d_task.dto.SessionDTO;
import com.example._d_task.repositories.SessionRepository;
import com.example._d_task.repositories.UserRepository;
import com.example._d_task.services.JWTService;
import com.example._d_task.services.SessionService;
import com.example._d_task.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path="/auth")
public class AuthController {

    private UserRepository userRepository;

    private UserService userService;

    private SessionRepository sessionRepository;

    private JWTService jwtService;

    private SessionService sessionService;

    public AuthController(UserRepository userRepository, UserService userService, SessionRepository sessionRepository,JWTService jwtService,SessionService sessionService){
        this.sessionRepository=sessionRepository;
        this.userRepository=userRepository;
        this.userService=userService;
        this.jwtService= jwtService;
        this.sessionService = sessionService;
    }

    @PostMapping(path="/register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO registerDTO) {
        if(userRepository.existsByEmail(registerDTO.getEmail())){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("This email already exists");
        }
        if(userRepository.existsByUsername(registerDTO.getUsername())){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("This username already exists");
        }


        userService.createUserFromRegister(registerDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body("User created ");
    }

    @PostMapping(path  = "/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO login){
        return sessionService.login(login) ;
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody SessionDTO dto){
        log.info("bebebe");
        return jwtService.initRefresh(dto.getRefreshToken());
    }

}
