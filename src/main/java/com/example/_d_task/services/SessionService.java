package com.example._d_task.services;


import com.example._d_task.dto.LoginDTO;
import com.example._d_task.dto.SessionDTO;
import com.example._d_task.models.SessionModel;
import com.example._d_task.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final UserRepository userRepository;

    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    public SessionService(UserRepository userRepository,PasswordEncoder passwordEncoder,JWTService jwtService){
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> login(LoginDTO login){
        if(!userRepository.existsByEmail(login.getEmail())){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Login Error 1");
        }
        if(passwordEncoder.matches(login.getPassHash(), userRepository.findByEmail(login.getEmail()).getPashHash())){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Login Error 2");
        }
        SessionDTO dto = new SessionDTO();
        String refreshToken = jwtService.createJWTRefresh(userRepository.findByEmail(login.getEmail()));
        String accessToken = jwtService.createAccessToken(refreshToken);
        dto.setRefreshToken(refreshToken);
        dto.setAccessToken(accessToken);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);
    }


    public SessionDTO sessionToDTO(SessionModel session){
        SessionDTO dto = new SessionDTO();
        dto.setAccessToken(session.getAccess_token());
        dto.setRefreshToken(session.getRefresh_token());
        dto.setAccess_exceeds_at(session.getAccess_exceeds_at());
        dto.setRefresh_exceeds_at(session.getRefresh_exceeds_at());
        return dto;
    }
}
