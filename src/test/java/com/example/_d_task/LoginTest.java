package com.example._d_task;

import com.example._d_task.dto.LoginDTO;
import com.example._d_task.dto.SessionDTO;
import com.example._d_task.enums.Role;
import com.example._d_task.models.UserModel;
import com.example._d_task.repositories.UserProjectRepository;
import com.example._d_task.repositories.UserRepository;
import com.example._d_task.services.JWTService;
import com.example._d_task.services.SessionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class LoginTest {
    @Autowired
    private SessionService sessionService;
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserProjectRepository userProjectRepository;
    @Autowired
    private JWTService jwtService;

    @BeforeEach
    public void setUp(){
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        sessionService = new SessionService(userRepository, passwordEncoder,Mockito.mock(JWTService.class));
    }


    @Test
    public void loginTestSucceed(){
        UserModel userAccount = new UserModel();
        userAccount.setEmail("paralich@gmail.com");
        userAccount.setPassHash(passwordEncoder.encode("123456789"));
        userAccount.setRole(Role.USER);
        userAccount.setUsername("paralich");

        Mockito.when(userRepository.existsByEmail("paralich@gmail.com")).thenReturn(true);
        Mockito.when(userRepository.findByEmail("paralich@gmail.com")).thenReturn(userAccount);

        LoginDTO login = new LoginDTO();
        login.setEmail("paralich@gmail.com");
        login.setPassHash("123456789");

        System.out.println(userRepository.findByEmail(login.getEmail()).getPashHash());
        System.out.println(passwordEncoder.matches(login.getPassHash(),userRepository.findByEmail(login.getEmail()).getPashHash()));
        ResponseEntity<?> response = sessionService.login(login);

        SessionDTO expectedBody = new SessionDTO();
        expectedBody.setAccessToken(null);
        expectedBody.setRefreshToken(null);
        Assertions.assertEquals(ResponseEntity.status(HttpStatus.ACCEPTED).body(expectedBody),sessionService.login(login));
        System.out.println(response.getBody());
    }

    @Test
    public void loginTestErrorWrongEmail(){
        UserModel userAccount = new UserModel();
        userAccount.setEmail("paralich@gmail.com");
        userAccount.setPassHash(passwordEncoder.encode("123456789"));
        userAccount.setRole(Role.USER);
        userAccount.setUsername("paralich");

        Mockito.when(userRepository.existsByEmail("paral1ch@gmail.com")).thenReturn(false);
        Mockito.when(userRepository.findByEmail("paralich@gmail.com")).thenReturn(userAccount);

        LoginDTO login = new LoginDTO();
        login.setEmail("paral1ch@gmail.com");
        login.setPassHash("123456789");

        ResponseEntity<?> response = sessionService.login(login);

        String expectedBody = "Login Error 1";
        Assertions.assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).body(expectedBody),sessionService.login(login));
        System.out.println(response.getBody());
    }

    @Test
    public void loginTestErrorWrongPassword(){
        UserModel userAccount = new UserModel();
        userAccount.setEmail("paralich@gmail.com");
        userAccount.setPassHash(passwordEncoder.encode("123456789"));
        userAccount.setRole(Role.USER);
        userAccount.setUsername("paralich");

        Mockito.when(userRepository.existsByEmail("paralich@gmail.com")).thenReturn(true);
        Mockito.when(userRepository.findByEmail("paralich@gmail.com")).thenReturn(userAccount);

        LoginDTO login = new LoginDTO();
        login.setEmail("paralich@gmail.com");
        login.setPassHash("1234567890");

        ResponseEntity<?> response = sessionService.login(login);

        String expectedBody = "Login Error 2";
        Assertions.assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).body(expectedBody),sessionService.login(login));
        System.out.println(response.getBody());
    }


}
