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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path="/auth")
public class AuthController {

    private final UserService userService;

    private final JWTService jwtService;

    private final SessionService sessionService;

    public AuthController(UserRepository userRepository, UserService userService, SessionRepository sessionRepository,JWTService jwtService,SessionService sessionService){
        this.userService=userService;
        this.jwtService= jwtService;
        this.sessionService = sessionService;
    }

    @PostMapping(path="/register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO registerDTO) {
        return userService.createUserFromRegister(registerDTO);
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
