package com.example._d_task.controllers;

import com.example._d_task.dto.ChangePasswordDTO;
import com.example._d_task.dto.UserDTO;
import com.example._d_task.models.UserModel;
import com.example._d_task.repositories.SessionRepository;
import com.example._d_task.repositories.UserRepository;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping(path="/Users")
public class UserController {

    private UserRepository userRepository;

    private SessionRepository sessionRepository;

    private UserService userService;

    public UserController(UserRepository userRepository, SessionRepository sessionRepository, UserService userService){
        this.userRepository = userRepository;
        this.userService = userService;
        this.sessionRepository = sessionRepository;
    }

    @GetMapping(path = "/AllUsers")
    public @ResponseBody Iterable<UserModel> getUsers(){
        return userRepository.findAll();
    }

    @PostMapping(path = "/logout")
    public @ResponseBody String logout(@RequestHeader("Authorization") String header){
        String token = header.substring(7);
        //sessionRepository.delete(sessionRepository.findByToken(token));
        return "Complete";
    }

    @GetMapping(path = "/profile")
    public @ResponseBody UserDTO profile(){
        if(!Auth.check()){return null;}
        return Auth.user().getUserDTO();
    }

    //@GetMapping(path = "/userByToken")
    //public @ResponseBody ResponseEntity<UserDTO> test(@RequestHeader("Authorization") String header){
    //    String token = header.substring(7);
    //    return ResponseEntity.ok(sessionRepository.findUserByToken(token).getUserDTO());
    //}


    @PostMapping(path = "/profile/edit")
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

    @PostMapping(path = "/profile/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO pass){
        if(!Objects.equals(Auth.user().getPashHash(), pass.getOldPassword())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error");
        }

        Auth.user().setPassHash(pass.getNewPassword());
        userRepository.save(Auth.user());
        return ResponseEntity.ok("Password changed");
    }
}
