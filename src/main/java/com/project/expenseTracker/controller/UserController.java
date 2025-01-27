package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.AuthRequest;
import com.project.expenseTracker.dto.UserDto;
import com.project.expenseTracker.dto.UserResponseDto;
import com.project.expenseTracker.security.JwtService;
import com.project.expenseTracker.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class UserController {

    private UserService userService;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder encoder;

    @PostMapping("/register")
    public String addNewUser(@RequestBody UserDto userDto) {
        userDto.setPassword(encoder.encode(userDto.getPassword()));
        return userService.addUser(userDto);
    }

    @PostMapping("/login")
    public UserResponseDto authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
        );
        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Invalid user request!");
        }
        String token = jwtService.generateToken(authRequest.getUserName());
        return userService.getUserResponse(authRequest, token);
    }
}
