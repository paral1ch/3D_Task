package com.example._d_task.Services;

import com.example._d_task.DTO.RegisterDTO;
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

        user.setDisplay_name(registerDTO.getDisplayName());
        user.setName(registerDTO.getUserName());
        user.setEmail(registerDTO.getEmail());
        user.setPassHash(registerDTO.getPasswordHash());
        userRepository.save(user);


    }



}
