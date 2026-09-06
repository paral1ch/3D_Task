package com.example._d_task.services;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example._d_task.dto.SessionDTO;
import com.example._d_task.enums.TokenTypes;
import com.example._d_task.models.UserModel;
import com.example._d_task.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

@Slf4j
@Service
public class JWTService {
    //Дни
    private static final int refreshLifespan = 15;
    //Минуты
    private static final int accessLifespan = 15;
    //Потом вынести в env
    private static final String JWTKEY = "JWTSECRETKEY";
    private static final Algorithm ALG = Algorithm.HMAC384(JWTKEY);
    private final String ISSUER = "3D-TASK";
    private final UserRepository userRepository;
    private final List<String> blacklist = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(JWTService.class.getName());
    public JWTService(UserRepository userRepository){
        this.userRepository = userRepository;
    };

    public  String createJWTRefresh(UserModel user){
        return JWT.create().withClaim("type", TokenTypes.REFRESH.name())
                .withClaim("id",user.getUserId())
                .withClaim("jti", UUID.randomUUID().toString())
                .withExpiresAt(Instant.now().plus(refreshLifespan, ChronoUnit.DAYS))
                .withIssuer(ISSUER).sign(ALG);
    }

    public String createAccessToken(String refresh_token){
        if(!checkSignature(refresh_token,TokenTypes.REFRESH.name())){
            log.info("Error during creating access token: " + checkSignature(refresh_token,TokenTypes.REFRESH.name()));
            return "Error";
        }
        UserModel user = userRepository.findByIdNullable(JWT.decode(refresh_token).getClaim("id").asInt());
        return JWT.create().withClaim("type",TokenTypes.ACCESS.name())
                .withClaim("id",user.getUserId())
                .withClaim("jti", UUID.randomUUID().toString())
                .withExpiresAt(Instant.now()
                        .plus(accessLifespan, ChronoUnit.MINUTES))
                .withIssuer(ISSUER).sign(ALG);
    }

    public ResponseEntity<?> initRefresh(String token){
        if(!checkSignature(token, TokenTypes.REFRESH.name())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error");
        }
        UserModel user = userRepository.findByIdNullable(JWT.decode(token).getClaim("id").asInt());
        SessionDTO dto = new SessionDTO();

        dto.setRefreshToken(createJWTRefresh(user));
        dto.setAccessToken(createAccessToken(dto.getRefreshToken()));
        return ResponseEntity.ok(dto);
    }

    public boolean checkSignature(String token, String expectedType){
        try{
            JWTVerifier verifier = JWT.require(ALG).withIssuer(ISSUER).build();
            DecodedJWT decoded = verifier.verify(token);
            if(!Objects.equals(decoded.getClaim("type").toString(),'"'+  expectedType+'"')){
                log.info("Wrong type " + decoded.getClaim("type").toString() + " " + expectedType);
                return false;
            }
            return !decoded.getExpiresAt().before(Date.from(Instant.now()));
        }
        catch(JWTVerificationException e){
            log.error("verification failed: {}", e.getMessage(),e);
            return false;
        }
    }
}
