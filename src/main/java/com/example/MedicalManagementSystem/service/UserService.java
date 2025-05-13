package com.example.MedicalManagementSystem.service;

import com.example.MedicalManagementSystem.model.User;
import com.example.MedicalManagementSystem.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User createUser(User user) {
        // Encode password before saving
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        user.setUsername(userDetails.getUsername());
        user.setRole(userDetails.getRole());

        // Only update password if provided
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            // Skip complex password validation for default accounts
            if (user.isDefaultAccount()) {
                // For default accounts, just ensure password is not empty
                if (!userDetails.getPassword().trim().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                }
            } else {
                // For regular accounts, password should already be validated
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
