package com.example._d_task.controllers;


import com.example._d_task.DTO.RegisterDTO;
import com.example._d_task.Services.UserService;
import com.example._d_task.models.SessionModel;
import com.example._d_task.models.UserModel;
import com.example._d_task.repositories.SessionRepository;
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
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionRepository sessionRepository;

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
    public ResponseEntity<String> login(@RequestBody RegisterDTO login){
        UserModel user = userRepository.findByEmail(login.getEmail());
        if(user==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong password or email (Wrong email)" + login.getEmail());
        }


        if(user.getPashHash() == login.getPasswordHash()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong password or email (Wrong password)");
        }

        String token = login.getEmail()+login.getUsername();

        SessionModel session = new SessionModel();
        session.setSessionToken(token);
        session.setEmail(login.getEmail());

        sessionRepository.save(session);

        return ResponseEntity.ok(token);
    }


}
