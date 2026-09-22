package com.example._d_task.controllers;

import com.example._d_task.dto.ChangePasswordDTO;
import com.example._d_task.dto.UserDTO;
import com.example._d_task.models.UserModel;
import com.example._d_task.repositories.UserRepository;
import com.example._d_task.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/Users")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    public UserController(UserRepository userRepository,  UserService userService){
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping(path = "/AllUsers")
    public @ResponseBody Iterable<UserModel> getUsers(){
        return userRepository.findAll();
    }

    //Потом крч сделать что б токен добавлялся в блеклист а не вот это вот все.
    //@PostMapping(path = "/logout")
    //public @ResponseBody String logout(@RequestHeader("Authorization") String header){
    //    String token = header.substring(7);
    //    //sessionRepository.delete(sessionRepository.findByToken(token));
    //    return "Complete";
    //}

    @GetMapping(path = "/profile")
    public @ResponseBody UserDTO profile(){
        return userService.profile();
    }

    //@GetMapping(path = "/userByToken")
    //public @ResponseBody ResponseEntity<UserDTO> test(@RequestHeader("Authorization") String header){
    //    String token = header.substring(7);
    //    return ResponseEntity.ok(sessionRepository.findUserByToken(token).getUserDTO());
    //}


    @PostMapping(path = "/profile/edit")
    public ResponseEntity<?> editProfile(UserDTO user){
        return userService.editProfile(user);
    }

    @PostMapping(path = "/profile/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO pass){
        return userService.changePassword(pass);
    }
}
