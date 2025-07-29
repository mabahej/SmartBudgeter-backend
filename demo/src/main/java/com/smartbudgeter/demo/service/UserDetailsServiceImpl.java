package com.smartbudgeter.demo.service;

import com.smartbudgeter.demo.model.User;
import com.smartbudgeter.demo.repository.UserRepository;
import com.smartbudgeter.demo.config.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username)  // change this to email
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    if (user.isDeleted()) {
        throw new UsernameNotFoundException("User is deleted");
    }
    System.out.println("Loading user by email: " + username);
    return new CustomUserDetails(
        user.getId(),
        user.getUsername(),
        user.getPassword(),
        Collections.singletonList(new SimpleGrantedAuthority("USER"))
    );
}

}
