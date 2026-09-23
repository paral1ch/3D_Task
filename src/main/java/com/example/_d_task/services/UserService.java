package com.example._d_task.services;

import com.example._d_task.dto.ChangePasswordDTO;
import com.example._d_task.dto.RegisterDTO;
import com.example._d_task.dto.UserDTO;
import com.example._d_task.enums.Role;
import com.example._d_task.models.UserModel;
import com.example._d_task.repositories.UserProjectRepository;
import com.example._d_task.repositories.UserRepository;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.servicesUtils.ModelToDTOConverters;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService {
    private final UserRepository userRepository;

    private final UserProjectRepository userProjectRepository;
    private final ModelToDTOConverters converters;
    public UserService(
            UserRepository userRepository,
            UserProjectRepository userProjectRepository,
            ModelToDTOConverters converters
    ){
        this.converters = converters;
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

    public UserDTO profile(){
        if(!Auth.check()){return null;}
        return Auth.user().getUserDTO();
    }

    public ResponseEntity<?> editProfile(UserDTO user){
        UserModel origUser = Auth.user();
        if(user.getUsername()!=origUser.getUsername() && userRepository.existsByUsername(user.getUsername())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Username already taken");
        }

        if(user.getEmail()!=origUser.getEmail() && userRepository.existsByEmail(user.getEmail())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email already exists");
        }

        origUser.setUsername(user.getUsername());
        origUser.setEmail(profile().getEmail());
        origUser.setFullName(user.getFullname());
        userRepository.save(origUser);
        return ResponseEntity.ok("Saved");
    }

    public ResponseEntity<?> changePassword(ChangePasswordDTO pass){
        if(!Objects.equals(Auth.user().getPashHash(), pass.getOldPassword())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error");
        }

        Auth.user().setPassHash(pass.getNewPassword());
        userRepository.save(Auth.user());
        return ResponseEntity.ok("Password changed");
    }

    public  Iterable<UserDTO> getUsers(){
        return converters.usersToDTO(userRepository.findAll());
    }
}
