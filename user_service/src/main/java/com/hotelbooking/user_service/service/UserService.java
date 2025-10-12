package com.hotelbooking.user_service.service;


import com.hotelbooking.user_service.model.User;
import com.hotelbooking.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;



    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User register(User user){
        if (userRepository.findUserByEmail(user.getEmail()).isPresent()){
            throw new RuntimeException("User exist");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }


    public boolean login(String email, String password){
        return userRepository.findUserByEmail(email)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

}
