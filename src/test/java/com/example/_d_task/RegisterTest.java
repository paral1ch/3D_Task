package com.example._d_task;


import com.example._d_task.dto.RegisterDTO;
import com.example._d_task.repositories.UserRepository;
import com.example._d_task.services.UserService;
import com.example._d_task.services.servicesUtils.ModelToDTOConverters;
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
public class RegisterTest {
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    @Autowired
    private ModelToDTOConverters converters;
    private UserService userService;


    @BeforeEach
    public void setUp(){
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        converters = Mockito.mock(ModelToDTOConverters.class);
        userService = new UserService(userRepository,converters,passwordEncoder);
    }

    @Test
    public void registerSucceed(){
        String email = "test@gmail.com";
        String username = "paralich";
        String fullname = "Kirill";
        String pass = "password 123";
        Mockito.when(userRepository.existsByEmail(email)).thenReturn(false);
        ResponseEntity<String> response = userService.createUserFromRegister(new RegisterDTO(fullname,username,email,pass));
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println("Registration test");
    }

    @Test
    public void registerErrorExistingEmail(){
        String email = "test@gmail.com";
        String username = "paralich";
        String fullname = "Kirill";
        String pass = "password 123";
        Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);

        ResponseEntity<String> response = userService.createUserFromRegister(new RegisterDTO(fullname,username,email,pass));
        Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        System.out.println("Registration test");
    }

    @Test
    public void registerErrorExistingUsername(){
        String email = "test@gmail.com";
        String username = "paralich";
        String fullname = "Kirill";
        String pass = "password 123";
        Mockito.when(userRepository.existsByUsername(username)).thenReturn(true);

        ResponseEntity<String> response = userService.createUserFromRegister(new RegisterDTO(fullname,username,email,pass));
        Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        System.out.println("Registration test");
    }
}
