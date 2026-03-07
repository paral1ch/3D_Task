package com.example._d_task.Services;

import com.example._d_task.DTO.RegisterDTO;
import com.example._d_task.ENUMS.Role;
import com.example._d_task.models.UserModel;
import com.example._d_task.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public void createUserFromRegister(RegisterDTO registerDTO){
        UserModel user = new UserModel();

        user.setUsername(registerDTO.getUsername());
        user.setFullName(registerDTO.getFullName());
        user.setEmail(registerDTO.getEmail());
        user.setPassHash(registerDTO.getPasswordHash());
        user.setRole(Role.USER);
        userRepository.save(user);
    }
}
