package com.athenura.contentflow.user.service;

import com.athenura.contentflow.user.entity.User;
import com.athenura.contentflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void deleteUser(Long id){
        User user = userRepository.findById(id).orElseThrow(()-> new RuntimeException("User not found!"));

        userRepository.delete(user);
    }
}
