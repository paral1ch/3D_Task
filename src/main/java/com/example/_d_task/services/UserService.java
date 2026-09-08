package com.example._d_task.services;

import com.example._d_task.dto.RegisterDTO;
import com.example._d_task.enums.Role;
import com.example._d_task.models.UserModel;
import com.example._d_task.repositories.UserProjectRepository;
import com.example._d_task.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    private final UserProjectRepository userProjectRepository;

    public UserService(
            UserRepository userRepository,
            UserProjectRepository userProjectRepository
    ){
        this.userRepository = userRepository;
        this.userProjectRepository = userProjectRepository;
    }

    public ResponseEntity<String> createUserFromRegister(RegisterDTO registerDTO){
        if(userRepository.existsByEmail(registerDTO.getEmail())){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("This email already exists");
        }
        if(userRepository.existsByUsername(registerDTO.getUsername())){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("This username already exists");
        }
        UserModel user = new UserModel();
        user.setUsername(registerDTO.getUsername());
        user.setFullName(registerDTO.getFullName());
        user.setEmail(registerDTO.getEmail());
        user.setPassHash(registerDTO.getPasswordHash());
        user.setRole(Role.USER);
        userRepository.save(user);
        return ResponseEntity.ok().body("User created");
    }

    public UserModel getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }






}
