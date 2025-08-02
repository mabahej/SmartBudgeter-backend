package com.smartbudgeter.demo.services;

import com.smartbudgeter.demo.models.User;
import com.smartbudgeter.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user by display name and handle Optional properly
        User user = userRepository.findByDisplayName(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with display name: " + username));
        
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getDisplayName())
            .password(user.getPassword())
            .authorities("USER")
            .build();
    }
}