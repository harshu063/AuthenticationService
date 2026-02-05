package com.example.authenticationservice.services;

import com.example.authenticationservice.exceptions.UserAlreadyExistsException;
import com.example.authenticationservice.exceptions.UserNotFoundException;
import com.example.authenticationservice.exceptions.WrongPasswordException;
import com.example.authenticationservice.models.Session;
import com.example.authenticationservice.models.SessionStatus;
import com.example.authenticationservice.models.User;
import com.example.authenticationservice.repositories.SessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.authenticationservice.repositories.UserRepository;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private SecretKey key = Jwts.SIG.HS256.key().build();
    public AuthService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, SessionRepository sessionRepository) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public boolean signUp(String email, String password) throws UserAlreadyExistsException {
    if(userRepository.findByEmail(email).isPresent()){
        throw new UserAlreadyExistsException("User with email" + email + " already exists");
    }
    User user = new User();
    user.setEmail(email);
    user.setPassword(bCryptPasswordEncoder.encode(password));
    userRepository.save(user);
        return true;
    }

    public String login(String email, String password) throws UserNotFoundException, WrongPasswordException {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(!userOptional.isPresent()){
            throw new UserNotFoundException("User with email" + email + "Not found");
        }
       boolean matches = bCryptPasswordEncoder.matches(password,userOptional.get().getPassword());
        if (matches){
            String token = createJwtToken(userOptional.get().getId(),
                    new ArrayList<>(),
                    userOptional.get().getEmail());
            Session session = new Session();
            session.setToken(token);
            session.setUser(userOptional.get());
            Date datePlus30 = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
            session.setExpiringAt(datePlus30);
            sessionRepository.save(session);

            return token;
        }
        else{
            throw new WrongPasswordException("User with password" +password + "is wrong");
        }

    }
    private String createJwtToken(Long user_Id, List<String> roles, String email){
        Map<String,Object> dataInJwt = new HashMap<>();
        dataInJwt.put("user_id",user_Id);
        dataInJwt.put("roles",roles);
        dataInJwt.put("email",email);
        Date datePlus30 = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
        String token = Jwts.builder()
                .setClaims(dataInJwt)
                .expiration(datePlus30)
                .issuedAt(new Date())
                .signWith(key)
                .compact();
                return token;
    }
    public boolean validate(String token){
        try{
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            Optional<Session> sessionOptional = sessionRepository.findByToken(token);
             if(!sessionOptional.isPresent()){
                 return false;
             }

        }catch (Exception e){
            return false;

        }

        return true;
    }

    public void logout(String token) {
        Optional<Session> sessionOptional = sessionRepository.findByToken(token);
        if(sessionOptional.isPresent()){
            Session session = sessionOptional.get();
            session.setSessionStatus(SessionStatus.ENDED);
            sessionRepository.save(session);
        }
    }
}
